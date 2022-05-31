# 学祭検温登録用BOT
## 概要
本プロジェクトはLineBotを通じて横国学祭実行委員内の体温登録作業を容易にするものです。
## 動作要件
### プロジェクトタイプ
Java Webアプリケーション
### 実行環境/要件
- Apache Tomcatでの動作。
- Google Spread Sheetを使うこと
- Google Spread Sheetでの名前欄は上から4行目とする
## 注意事項(今後この要件を解除予定)
- 現状では編集部での利用のみを想定しています。
- Google Spread Sheetの名前は`編集現役`としてください。
## セットアップ
### Line Messagingでの設定
Line DevelopersでLineBotアカウントを作成。以下の設定をする。
- BotのWebhook設定を以下のように設定<br>
`(WebアプリケーションのBASE URL)/line`
- オートリプライ設定、Greeting MessagesをOffに
- (一応確認)応答モードがWebhookになっているかどうか

### Google Spread Sheetでの設定
- 対象のスプレッドシートにて`Extensions>Google App Script`を開き、
[このファイルのコード](GASCode.js)をデプロイする。
- デプロイした時に表示されるURLをコピー、`Apache Tomcatの設定`の設定ファイル作成にて使う。
### Apache Tomcatでの設定

1. 次の場所に設定ファイルを作る。<br>
`(Apache Tomcat実行ユーザーのuser.home)/ShionServerConfig/YNUFES-Design-BodyTempBot/config.properties`<br>
```
GASUrl=(Google App Scriptの実行URL)
BotToken=(LineMessagingで発行したChannelAccessToken)
```
2. ビルドしてWARファイルを生成。
3. Apache TomcatのWebappsに、ビルドしたWARファイルをデプロイする
## 使い方
Lineチャット上での操作は次の通りです。
登録に失敗した場合はエラーが表示されます。