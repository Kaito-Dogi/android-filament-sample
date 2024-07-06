// https://github.com/google/filament/blob/main/android/samples/sample-hello-triangle/src/main/java/com/google/android/filament/hellotriangle/MainActivity.kt

package app.doggy.filamentsample

import android.animation.ValueAnimator
import android.app.Activity
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import android.view.animation.LinearInterpolator
import com.google.android.filament.Box
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.Entity
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.RenderableManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.SwapChainFlags
import com.google.android.filament.VertexBuffer
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.FilamentHelper
import com.google.android.filament.android.UiHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : Activity() {
  // Filament を初期化
  // ほとんどの API 呼び出しに必要な JNI ライブラリをロードする
  companion object {
    init {
      Filament.init()
    }
  }

  // 描画したい View（今回は SurfaceView）
  private lateinit var surfaceView: SurfaceView

  // UiHelper
  // SurfaceView や SurfaceTexture を管理する
  private lateinit var uiHelper: UiHelper

  // DisplayHelper
  // ディスプレイを管理する
  private lateinit var displayHelper: DisplayHelper

  // Choreographer
  // 新しいフレームをスケジュールするために使用されるする
  private lateinit var choreographer: Choreographer

  // Engine
  // Filament リソースの作成と破棄を行う
  // 各 Engine は、選択した単一のスレッドからアクセスする必要がある
  // リソースは Engine 間で共有できない
  // リソース
  // - Entity
  // - Renderer
  // - Scene
  // - View
  // - VertexBuffer, IndexBuffer, Material など
  private lateinit var engine: Engine

  // Renderer
  // 単一の Surface（SurfaceView や TextureView など）に結び付けられる
  private lateinit var renderer: Renderer

  // Scene
  // すべての描画可能オブジェクトやライトなどを保持する
  private lateinit var scene: Scene

  // View
  // ビューポート、シーン、カメラを定義し、描画する
  private lateinit var view: View

  // Camera
  private lateinit var camera: Camera

  private lateinit var material: Material
  private lateinit var vertexBuffer: VertexBuffer
  private lateinit var indexBuffer: IndexBuffer

  // Filament Entity
  // 描画可能オブジェクトを表す
  @Entity
  private var renderable = 0

  // SwapChain
  // Filament の Surface の表現
  private var swapChain: SwapChain? = null

  // レンダリングを行い、新しいフレームをスケジュールする
  private val frameScheduler = FrameCallback()

  private val animator = ValueAnimator.ofFloat(0.0f, 360.0f)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    surfaceView = SurfaceView(this)
    setContentView(surfaceView)

    choreographer = Choreographer.getInstance()

    displayHelper = DisplayHelper(this)

    setupSurfaceView()
    setupFilament()
    setupView()
    setupScene()
  }

  private fun setupSurfaceView() {
    uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    uiHelper.renderCallback = SurfaceCallback()

    // 特定のレンダリング解像度を選択するには、次の行のコメントを実行する
    // uiHelper.setDesiredSize(1280, 720)
    uiHelper.attachTo(surfaceView)
  }

  private fun setupFilament() {
    engine = Engine.Builder().featureLevel(Engine.FeatureLevel.FEATURE_LEVEL_0).build()
    renderer = engine.createRenderer()
    scene = engine.createScene()
    view = engine.createView()
    camera = engine.createCamera(engine.entityManager.create())
  }

  private fun setupView() {
    scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(engine)

    // post-processing はフィーチャーレベル0ではサポートされていない
    view.isPostProcessingEnabled = false

    // View に使用する Camera を指定する
    view.camera = camera

    // 描画する Scene を View に指定する
    view.scene = scene
  }

  private fun setupScene() {
    loadMaterial()
    createMesh()

    // 描画可能オブジェクトを作成するため、最初に一般的なエンティティを作成する
    renderable = EntityManager.get().create()

    // 続いて、そのエンティティに描画可能コンポーネントを作成する
    // 描画可能オブジェクトは複数のプリミティブで構成される（今回は1つだけを宣言する）
    RenderableManager.Builder(1)
      // 描画可能オブジェクトの全体の領域（Bounding Box）
      .boundingBox(Box(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.01f))
      // 1つ目のプリミティブのメッシュデータを設定する
      .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer, 0, 3)
      // 1つ目のプリミティブのマテリアルを設定する
      .material(0, material.defaultInstance)
      .build(engine, renderable)

    // Scene に Entity を追加し、描画する
    scene.addEntity(renderable)

    startAnimation()
  }

  private fun loadMaterial() {
    readUncompressedAsset("filamat/baked_color.filamat").let {
      material = Material.Builder().payload(it, it.remaining()).build(engine)
      material.compile(
        Material.CompilerPriorityQueue.HIGH,
        Material.UserVariantFilterBit.ALL,
        Handler(Looper.getMainLooper()),
      ) {
        android.util.Log.i(
          "hellotriangle",
          "Material " + material.name + " compiled.",
        )
      }
      engine.flush()
    }
  }

  private fun createMesh() {
    val intSize = 4
    val floatSize = 4
    val shortSize = 2
    // 頂点は位置と色で構成される
    // XYZ 座標に3つの浮動小数点、色に1つの整数
    val vertexSize = 3 * floatSize + intSize

    // 頂点を定義
    data class Vertex(val x: Float, val y: Float, val z: Float, val color: Int)

    // ByteBuffer に頂点を入れる関数
    fun ByteBuffer.put(v: Vertex): ByteBuffer {
      putFloat(v.x)
      putFloat(v.y)
      putFloat(v.z)
      putInt(v.color)
      return this
    }

    // 単一の三角形を生成する
    val vertexCount = 3
    val a1 = PI * 2.0 / 3.0
    val a2 = PI * 4.0 / 3.0

    val vertexData = ByteBuffer.allocate(vertexCount * vertexSize)
      // ネイティブのバイトオーダーを遵守する
      .order(ByteOrder.nativeOrder())
      .put(Vertex(1.0f, 0.0f, 0.0f, 0xffff0000.toInt()))
      .put(Vertex(cos(a1).toFloat(), sin(a1).toFloat(), 0.0f, 0xff00ff00.toInt()))
      .put(Vertex(cos(a2).toFloat(), sin(a2).toFloat(), 0.0f, 0xff0000ff.toInt()))
      // ByteBuffer のカーソルが正しい位置にあることを確認する
      .flip()

    // メッシュのレイアウトを宣言する
    vertexBuffer = VertexBuffer.Builder()
      .bufferCount(1)
      .vertexCount(vertexCount)
      // 位置と色のデータを交互に配置しているため、オフセットとストライドを指定する必要がある
      // - オフセット：各属性が頂点データ内でどこに存在するかを示す
      // - ストライド：各頂点データが、バッファ内でどのくらいの間隔で繰り返されるかを示す
      // 2つのバッファを宣言して、各属性に異なる Buffer Index を与えることで データを非交差化できる
      .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, vertexSize)
      .attribute(VertexBuffer.VertexAttribute.COLOR, 0, VertexBuffer.AttributeType.UBYTE4, 3 * floatSize, vertexSize)
      // 色を符号なしバイトとして保存するが、マテリアル（シェーダー）では0から1の値を使用するため、属性を正規化としてマークする必要がある
      .normalized(VertexBuffer.VertexAttribute.COLOR)
      .build(engine)

    // メッシュに頂点データを入力する
    // データが交差しているため、1つのバッファしか設定しない
    vertexBuffer.setBufferAt(engine, 0, vertexData)

    // インデックスを作成する
    val indexData = ByteBuffer.allocate(vertexCount * shortSize)
      .order(ByteOrder.nativeOrder())
      .putShort(0)
      .putShort(1)
      .putShort(2)
      .flip()

    indexBuffer = IndexBuffer.Builder()
      .indexCount(3)
      .bufferType(IndexBuffer.Builder.IndexType.USHORT)
      .build(engine)
    indexBuffer.setBuffer(engine, indexData)
  }

  private fun startAnimation() {
    // 三角形をアニメーション化
    animator.interpolator = LinearInterpolator()
    animator.duration = 4000
    animator.repeatMode = ValueAnimator.RESTART
    animator.repeatCount = ValueAnimator.INFINITE
    animator.addUpdateListener(
      object : ValueAnimator.AnimatorUpdateListener {
        val transformMatrix = FloatArray(16)
        override fun onAnimationUpdate(a: ValueAnimator) {
          Matrix.setRotateM(transformMatrix, 0, -(a.animatedValue as Float), 0.0f, 0.0f, 1.0f)
          val tcm = engine.transformManager
          tcm.setTransform(tcm.getInstance(renderable), transformMatrix)
        }
      },
    )
    animator.start()
  }

  override fun onResume() {
    super.onResume()
    choreographer.postFrameCallback(frameScheduler)
    animator.start()
  }

  override fun onPause() {
    super.onPause()
    choreographer.removeFrameCallback(frameScheduler)
    animator.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()

    // アニメーションと、保留中のフレームを停止する
    choreographer.removeFrameCallback(frameScheduler)
    animator.cancel()

    // Engine を破棄する前に、常に Surface をデタッチする
    uiHelper.detach()

    // すべてのリソースをクリーンアップする
    engine.destroyEntity(renderable)
    engine.destroyRenderer(renderer)
    engine.destroyVertexBuffer(vertexBuffer)
    engine.destroyIndexBuffer(indexBuffer)
    engine.destroyMaterial(material)
    engine.destroyView(view)
    engine.destroyScene(scene)
    engine.destroyCameraComponent(camera.entity)

    // Engine.destroyEntity() は Filament に関連するリソースのみを破棄し、エンティティ自体は破棄しない
    // そのため、Entity は EntityManager 経由で破棄する
    val entityManager = EntityManager.get()
    entityManager.destroy(renderable)
    entityManager.destroy(camera.entity)

    // エンジンを破棄すると破棄し忘れたリソースも解放されるが、適切にクリーンアップすることが推奨されている
    engine.destroy()
  }

  inner class FrameCallback : Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      // 次のフレームをスケジュールする
      choreographer.postFrameCallback(this)

      // SwapChain があることを確認
      if (uiHelper.isReadyToRender) {
        // beginFrame() が false を返す場合、フレームをスキップする必要がある
        // これは GPU にフレームを送信する速度が速すぎることを意味する
        if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
          renderer.render(view)
          renderer.endFrame()
        }
      }
    }
  }

  inner class SurfaceCallback : UiHelper.RendererCallback {
    override fun onNativeWindowChanged(surface: Surface) {
      swapChain?.let { engine.destroySwapChain(it) }

      // フィーチャーレベル0では post-processing がないため、カラースペースを sRGB に設定する必要がある（これはすべての場所でサポートされているわけではない）
      var flags = uiHelper.swapChainFlags
      if (engine.activeFeatureLevel == Engine.FeatureLevel.FEATURE_LEVEL_0) {
        if (SwapChain.isSRGBSwapChainSupported(engine)) {
          flags = flags or SwapChainFlags.CONFIG_SRGB_COLORSPACE
        }
      }

      swapChain = engine.createSwapChain(surface, flags)
      displayHelper.attach(renderer, surfaceView.display)
    }

    override fun onDetachedFromSurface() {
      displayHelper.detach()
      swapChain?.let {
        engine.destroySwapChain(it)
        // Filament が、destroySwapChain コマンドを実行し終わる前に戻らないようにするために必要
        // これがないと、Android が Surface を早く破棄する可能性がある
        engine.flushAndWait()
        swapChain = null
      }
    }

    override fun onResized(width: Int, height: Int) {
      val zoom = 1.5
      val aspect = width.toDouble() / height.toDouble()
      camera.setProjection(
        Camera.Projection.ORTHO,
        -aspect * zoom, aspect * zoom, -zoom, zoom, 0.0, 10.0,
      )

      view.viewport = Viewport(0, 0, width, height)

      FilamentHelper.synchronizePendingFrames(engine)
    }
  }

  private fun readUncompressedAsset(assetName: String): ByteBuffer {
    // assets.openFd(assetName).use { fd ->
    //   val input = fd.createInputStream()
    //   val dst = ByteBuffer.allocate(fd.length.toInt())
    //
    //   val src = Channels.newChannel(input)
    //   src.read(dst)
    //   src.close()
    //
    //   return dst.apply { rewind() }
    // }
    // FileDescriptor を使わずに、通常のストリームを使ってファイルを読み込む
    assets.open(assetName).use { input ->
      val byteArray = input.readBytes()
      val buffer = ByteBuffer.allocate(byteArray.size)
      buffer.put(byteArray)
      return buffer.apply { rewind() }
    }
  }
}
