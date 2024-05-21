# AndoDatasetCollectionSystemとは
<img src=img/top.png>

## 車両の走行データを記録するAndroidアプリケーションです

スポーツ走行、ツーリングの記録に使える、走行データ記録アプリです。

OBD2を用いて、車載センサーにアクセスすることで、走行データを収集します。

※OBD(On-Board Diagnostics) 車載診断機のこと。最近、車検の検査項目にOBDを使うようになったので、どの車にもあると思う。

前提条件として、
1. OBD対応車種
2. Android機器
3. OBD信号をBluetoothに変換するトグルを用いること

主にスポーツ走行において、後から操作を分析するためのツールとして活用することを想定しています。

## 計測できる項目

 - スロットル開度(OBD)
 - エンジン回転数(OBD)
 - 緯度、経度(スマホGPS)
 - 時間(スマホ内時計)

# 動作環境

- スマホ機種：Android Pixel 6a
- OBD接続機器：OBD2-Bluetoothトグル　https://amzn.asia/d/3xTakmA
- バイク：GSX-R125



# 機能紹介

- CSV形式で保存
- 走行ログ表示

# 今後の予定

バンク角測定
