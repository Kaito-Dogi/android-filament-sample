#!/bin/bash

# プロジェクトのルートディレクトリへの相対パス
PROJECT_ROOT=$(dirname "$0")/..
FILAMENT_BIN="$PROJECT_ROOT/tools/filament/bin/matc"
MAT_DIR="$PROJECT_ROOT/app/src/main/assets/mat"
FILAMAT_DIR="$PROJECT_ROOT/app/src/main/assets/filamat"

# filamat ディレクトリが存在しなかったら作成する
if [ ! -d "$FILAMAT_DIR" ]; then
  mkdir -p "$FILAMAT_DIR"
  echo "Created directory: $FILAMAT_DIR"
fi

# mat ディレクトリにあるすべての .mat ファイルを処理する
for mat_file in "$MAT_DIR"/*.mat; do
  if [ -f "$mat_file" ]; then
    filename=$(basename "$mat_file" .mat)
    output_file="$FILAMAT_DIR/$filename.filamat"
    echo "Processing $mat_file -> $output_file"
    "$FILAMENT_BIN" -o "$output_file" "$mat_file"
  fi
done

echo "すべての .mat ファイルが .filamat ファイルにコンパイルされました。"
