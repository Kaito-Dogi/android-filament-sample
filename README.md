# Filament Sample Project

Android アプリで Filament を使用するためのサンプルアプリです。

## 環境構築

1. google/filament の [Releases](https://github.com/google/filament/releases) から最新の Filament をダウンロード
    - macOS で開発する場合は filament-vx.y.z-mac.tgz
2. tools ディレクトリに Filament を配置
3. mat ファイルを filamat ファイルにコンパイル

```sh
./tools/compile_mat_to_filamat.sh
```

## 参考リンク

- [google/filament]()
  - [samples/hello-triangle](https://github.com/google/filament/tree/main/android/samples/sample-hello-triangle)
  - [samples/texture-view](https://github.com/google/filament/tree/main/android/samples/sample-texture-view)
  - [samples/gltf-viewer](https://github.com/google/filament/tree/main/android/samples/sample-gltf-viewer)
- [Androidでリッチな3DCGを扱う〜Google Filament事始め〜](https://note.com/navitime_tech/n/n71cd3e3d3c7f)
- [java.io.FileNotFoundException: This file can not be opened as a file descriptor; it is probably compressed](https://github.com/google/filament/discussions/5696)
