// https://github.com/google/filament/blob/main/android/samples/sample-gltf-viewer/src/main/java/com/google/android/filament/gltf/MainActivity.kt

package app.doggy.filamentsample.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import app.doggy.filamentsample.R
import com.google.android.filament.Fence
import com.google.android.filament.IndirectLight
import com.google.android.filament.Material
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.utils.AutomationEngine
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.IBLPrefilterContext
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.RemoteServer
import com.google.android.filament.utils.Utils
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GltfViewerActivity : Activity() {
  companion object {
    fun newIntent(context: Context) = Intent(context, GltfViewerActivity::class.java)

    // Utility レイヤーのライブラリをロードする
    // gltfio と Filament のコアもロードされる
    init {
      Utils.init()
    }

    private const val TAG = "gltf-viewer"
  }

  private lateinit var surfaceView: SurfaceView
  private lateinit var choreographer: Choreographer
  private val frameScheduler = FrameCallback()
  private lateinit var modelViewer: ModelViewer
  private lateinit var titlebarHint: TextView
  private val doubleTapListener = DoubleTapListener()
  private val singleTapListener = SingleTapListener()
  private lateinit var doubleTapDetector: GestureDetector
  private lateinit var singleTapDetector: GestureDetector
  private var remoteServer: RemoteServer? = null
  private var statusToast: Toast? = null
  private var statusText: String? = null
  private var latestDownload: String? = null
  private val automation = AutomationEngine()
  private var loadStartTime = 0L
  private var loadStartFence: Fence? = null
  private val viewerContent = AutomationEngine.ViewerContent()

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_gltf_viewer)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    titlebarHint = findViewById(R.id.user_hint)
    surfaceView = findViewById(R.id.main_sv)
    choreographer = Choreographer.getInstance()

    doubleTapDetector = GestureDetector(applicationContext, doubleTapListener)
    singleTapDetector = GestureDetector(applicationContext, singleTapListener)

    modelViewer = ModelViewer(surfaceView)
    viewerContent.view = modelViewer.view
    viewerContent.sunlight = modelViewer.light
    viewerContent.lightManager = modelViewer.engine.lightManager
    viewerContent.scene = modelViewer.scene
    viewerContent.renderer = modelViewer.renderer

    surfaceView.setOnTouchListener { _, event ->
      modelViewer.onTouchEvent(event)
      doubleTapDetector.onTouchEvent(event)
      singleTapDetector.onTouchEvent(event)
      true
    }

    createDefaultRenderables()
    createIndirectLight()

    setStatusText("新しいモデルを読み込むには、ホストマシンで上記の URL にアクセスしてください")

    val view = modelViewer.view

    /**
     * 注: 以下の設定はリモート UI に接続すると上書きされる
     */

    // モバイルでは、低品質のカラーバッファを使用する
    view.renderQuality = view.renderQuality.apply {
      hdrColorBuffer = View.QualityLevel.MEDIUM
    }

    // Dynamic Resolution（動的解像度）が役立つ
    view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
      enabled = true
      quality = View.QualityLevel.MEDIUM
    }

    // 動的解像度 MEDIUM では MSAA が必要
    view.multiSampleAntiAliasingOptions = view.multiSampleAntiAliasingOptions.apply {
      enabled = true
    }

    // FXAA は低コストだが効果的
    view.antiAliasing = View.AntiAliasing.FXAA

    // Ambient Occlusion は低コストながら、クオリティをかなり向上させる
    view.ambientOcclusionOptions = view.ambientOcclusionOptions.apply {
      enabled = true
    }

    // Bloom は高コストだが、リアリティをかなり向上させる
    view.bloomOptions = view.bloomOptions.apply {
      enabled = true
    }

    remoteServer = RemoteServer(8082)
  }

  private fun createDefaultRenderables() {
    val buffer = assets.open("models/BusterDrone/scene.gltf").use { input ->
      val bytes = ByteArray(input.available())
      input.read(bytes)
      ByteBuffer.wrap(bytes)
    }

    modelViewer.loadModelGltfAsync(buffer) { uri -> readCompressedAsset("models/BusterDrone/$uri") }
    updateRootTransform()
  }

  private fun createIndirectLight() {
    val engine = modelViewer.engine
    val scene = modelViewer.scene
    val ibl = "default"
    readCompressedAsset("ktx/env/$ibl/ibl.ktx").let {
      scene.indirectLight = KTX1Loader.createIndirectLight(engine, it)
      scene.indirectLight!!.intensity = 30_000.0f
      viewerContent.indirectLight = modelViewer.scene.indirectLight
    }
    readCompressedAsset("ktx/env/$ibl/skybox.ktx").let {
      scene.skybox = KTX1Loader.createSkybox(engine, it)
    }
  }

  private fun readCompressedAsset(assetName: String): ByteBuffer {
    val input = assets.open(assetName)
    val bytes = ByteArray(input.available())
    input.read(bytes)
    return ByteBuffer.wrap(bytes)
  }

  private fun clearStatusText() {
    statusToast?.let {
      it.cancel()
      statusText = null
    }
  }

  private fun setStatusText(text: String) {
    runOnUiThread {
      if (statusToast == null || statusText != text) {
        statusText = text
        statusToast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        statusToast!!.show()
      }
    }
  }

  private suspend fun loadGlb(message: RemoteServer.ReceivedMessage) {
    withContext(Dispatchers.Main) {
      modelViewer.destroyModel()
      modelViewer.loadModelGlb(message.buffer)
      updateRootTransform()
      loadStartTime = System.nanoTime()
      loadStartFence = modelViewer.engine.createFence()
    }
  }

  private suspend fun loadHdr(message: RemoteServer.ReceivedMessage) {
    withContext(Dispatchers.Main) {
      val engine = modelViewer.engine
      val equirect = HDRLoader.createTexture(engine, message.buffer)
      if (equirect == null) {
        setStatusText("HDR ファイルのデコードに失敗しました")
      } else {
        setStatusText("HDR ファイルのデコードに成功しました")

        val context = IBLPrefilterContext(engine)
        val equirectToCubemap = IBLPrefilterContext.EquirectangularToCubemap(context)
        val skyboxTexture = equirectToCubemap.run(equirect)!!
        engine.destroyTexture(equirect)

        val specularFilter = IBLPrefilterContext.SpecularFilter(context)
        val reflections = specularFilter.run(skyboxTexture)

        val ibl = IndirectLight.Builder()
          .reflections(reflections)
          .intensity(30000.0f)
          .build(engine)

        val sky = Skybox.Builder().environment(skyboxTexture).build(engine)

        specularFilter.destroy()
        equirectToCubemap.destroy()
        context.destroy()

        // 前の IBL を破棄する
        engine.destroyIndirectLight(modelViewer.scene.indirectLight!!)
        engine.destroySkybox(modelViewer.scene.skybox!!)

        modelViewer.scene.skybox = sky
        modelViewer.scene.indirectLight = ibl
        viewerContent.indirectLight = ibl
      }
    }
  }

  private suspend fun loadZip(message: RemoteServer.ReceivedMessage) {
    // メモリ圧迫を緩和するため、zip の解凍前に古いモデルを削除する
    withContext(Dispatchers.Main) {
      modelViewer.destroyModel()
    }

    // 大きな zip ファイルでは OOM を防ぐため、まずファイルに書き出すべき
    // また、メッセージの buffer フィールドを null にすることも重要
    val (zipStream, zipFile) = withContext(Dispatchers.IO) {
      val file = File.createTempFile("incoming", "zip", cacheDir)
      val raf = RandomAccessFile(file, "rw")
      raf.channel.write(message.buffer)
      message.buffer = null
      raf.seek(0)
      Pair(FileInputStream(file), file)
    }

    // IO ディスパッチャ を使用して、各リソースを一つずつ解凍する
    var gltfPath: String? = null
    var outOfMemory: String? = null
    val pathToBufferMapping = withContext(Dispatchers.IO) {
      val deflater = ZipInputStream(zipStream)
      val mapping = HashMap<String, Buffer>()
      while (true) {
        val entry = deflater.nextEntry ?: break
        if (entry.isDirectory) continue

        // zip ファイルを汚染することが多い不要なファイルを無視する（厳密には必要ない）
        if (entry.name.startsWith("__MACOSX")) continue
        if (entry.name.startsWith(".DS_Store")) continue

        val uri = entry.name
        val byteArray: ByteArray? = try {
          deflater.readBytes()
        } catch (e: OutOfMemoryError) {
          outOfMemory = uri
          break
        }
        Log.i(TAG, "Deflated ${byteArray!!.size} bytes from $uri")
        val buffer = ByteBuffer.wrap(byteArray)
        mapping[uri] = buffer
        if (uri.endsWith(".gltf") || uri.endsWith(".glb")) {
          gltfPath = uri
        }
      }
      mapping
    }

    zipFile.delete()

    if (gltfPath == null) {
      setStatusText(".gltf または .glb が見つかりませんでした")
      return
    }

    if (outOfMemory != null) {
      setStatusText("$outOfMemory の解凍中にメモリ不足になりました")
      return
    }

    val gltfBuffer = pathToBufferMapping[gltfPath]!!

    // zip ファイル内の gltf ファイルは、リソースと同じフォルダにある場合も、異なるフォルダにある場合もあり、どちらのケースもテストすることが重要
    // いずれの場合も、リソースパスは gltf ファイルの場所に対して相対的に指定される
    var prefix = URI(gltfPath!!).resolve(".")

    withContext(Dispatchers.Main) {
      if (gltfPath!!.endsWith(".glb")) {
        modelViewer.loadModelGlb(gltfBuffer)
      } else {
        modelViewer.loadModelGltf(gltfBuffer) { uri ->
          val path = prefix.resolve(uri).toString()
          if (!pathToBufferMapping.contains(path)) {
            Log.e(TAG, "Could not find '$uri' in zip using prefix '$prefix' and base path '${gltfPath!!}'")
            setStatusText("Zip に $path が見つかりません")
          }
          pathToBufferMapping[path]
        }
      }
      updateRootTransform()
      loadStartTime = System.nanoTime()
      loadStartFence = modelViewer.engine.createFence()
    }
  }

  override fun onResume() {
    super.onResume()
    choreographer.postFrameCallback(frameScheduler)
  }

  override fun onPause() {
    super.onPause()
    choreographer.removeFrameCallback(frameScheduler)
  }

  override fun onDestroy() {
    super.onDestroy()
    choreographer.removeFrameCallback(frameScheduler)
    remoteServer?.close()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  fun loadModelData(message: RemoteServer.ReceivedMessage) {
    Log.i(TAG, "Downloaded model ${message.label} (${message.buffer.capacity()} bytes)")
    clearStatusText()
    titlebarHint.text = message.label
    CoroutineScope(Dispatchers.IO).launch {
      when {
        message.label.endsWith(".zip") -> loadZip(message)
        message.label.endsWith(".hdr") -> loadHdr(message)
        else -> loadGlb(message)
      }
    }
  }

  fun loadSettings(message: RemoteServer.ReceivedMessage) {
    val json = StandardCharsets.UTF_8.decode(message.buffer).toString()
    viewerContent.assetLights = modelViewer.asset?.lightEntities
    automation.applySettings(modelViewer.engine, json, viewerContent)
    modelViewer.view.colorGrading = automation.getColorGrading(modelViewer.engine)
    modelViewer.cameraFocalLength = automation.viewerOptions.cameraFocalLength
    modelViewer.cameraNear = automation.viewerOptions.cameraNear
    modelViewer.cameraFar = automation.viewerOptions.cameraFar
    updateRootTransform()
  }

  private fun updateRootTransform() {
    if (automation.viewerOptions.autoScaleEnabled) {
      modelViewer.transformToUnitCube()
    } else {
      modelViewer.clearRootTransform()
    }
  }

  inner class FrameCallback : Choreographer.FrameCallback {
    private val startTime = System.nanoTime()
    override fun doFrame(frameTimeNanos: Long) {
      choreographer.postFrameCallback(this)

      loadStartFence?.let {
        if (it.wait(Fence.Mode.FLUSH, 0) == Fence.FenceStatus.CONDITION_SATISFIED) {
          val end = System.nanoTime()
          val total = (end - loadStartTime) / 1_000_000
          Log.i(TAG, "The Filament backend took $total ms to load the model geometry.")
          modelViewer.engine.destroyFence(it)
          loadStartFence = null

          val materials = mutableSetOf<Material>()
          val rcm = modelViewer.engine.renderableManager
          modelViewer.scene.forEach {
            val entity = it
            if (rcm.hasComponent(entity)) {
              val ri = rcm.getInstance(entity)
              val c = rcm.getPrimitiveCount(ri)
              for (i in 0 until c) {
                val mi = rcm.getMaterialInstanceAt(ri, i)
                val ma = mi.material
                materials.add(ma)
              }
            }
          }
          materials.forEach {
            it.compile(
              Material.CompilerPriorityQueue.HIGH,
              Material.UserVariantFilterBit.DIRECTIONAL_LIGHTING or
                Material.UserVariantFilterBit.DYNAMIC_LIGHTING or
                Material.UserVariantFilterBit.SHADOW_RECEIVER,
              null, null,
            )
            it.compile(
              Material.CompilerPriorityQueue.LOW,
              Material.UserVariantFilterBit.FOG or
                Material.UserVariantFilterBit.SKINNING or
                Material.UserVariantFilterBit.SSR or
                Material.UserVariantFilterBit.VSM,
              null, null,
            )
          }
        }
      }

      modelViewer.animator?.apply {
        if (animationCount > 0) {
          val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
          applyAnimation(0, elapsedTimeSeconds.toFloat())
        }
        updateBoneMatrices()
      }

      modelViewer.render(frameTimeNanos)

      // 新しいダウンロードが進行中かどうか確認する
      val currentDownload = remoteServer?.peekIncomingLabel()
      if (RemoteServer.isBinary(currentDownload) && currentDownload != latestDownload) {
        latestDownload = currentDownload
        Log.i(TAG, "Downloading $currentDownload")
        setStatusText("Downloading $currentDownload")
      }

      // 新しいメッセージが完全に受信されたかどうか確認する
      val message = remoteServer?.acquireReceivedMessage()
      if (message != null) {
        if (message.label == latestDownload) {
          latestDownload = null
        }
        if (RemoteServer.isJson(message.label)) {
          loadSettings(message)
        } else {
          loadModelData(message)
        }
      }
    }
  }

  // テストのため、現在のモデルを解放して、デフォルトのモデルを再読み込みする
  inner class DoubleTapListener : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent): Boolean {
      modelViewer.destroyModel()
      createDefaultRenderables()
      return super.onDoubleTap(e)
    }
  }

  // テスト用
  inner class SingleTapListener : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapUp(event: MotionEvent): Boolean {
      modelViewer.view.pick(
        event.x.toInt(),
        surfaceView.height - event.y.toInt(),
        surfaceView.handler,
      ) {
        val name = modelViewer.asset!!.getName(it.renderable)
        Log.v("Filament", "Picked ${it.renderable}: " + name)
      }
      return super.onSingleTapUp(event)
    }
  }
}
