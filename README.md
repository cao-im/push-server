# push-server

CaoIM 离线推送服务。端口 **8083**（8082 是 im-admin）。设备令牌和发送记录放在 `cao_im_db` 的 `push_device`、`push_log`。

它只保存设备令牌，并在 im-server 判定用户不在线时投递一条系统通知。消息正文、好友关系和 WebSocket 仍在 im-server。

## 启动

1. 先有 `cao_im_db`（执行过 `im-server/sql/schema.sql` 即可，其中已包含推送表）。已有旧库时再执行 `sql/schema.sql`。
2. 在 im-server 打开：

```yaml
caoim:
  offline-push:
    enabled: true
    url: http://127.0.0.1:8083
    internal-token: caoim-dev-internal-token
```

3. 在 app-server 打开：

```yaml
push:
  server:
    enabled: true
    url: http://127.0.0.1:8083
    internal-token: caoim-dev-internal-token
```

4. `mvn -DskipTests package` 后运行本服务。

默认 `push.delivery=log`：通知会写入 `push_log`，手机不会响。

接友盟时改成 `delivery: umeng`，并填写 Android、iOS 的 AppKey 和 App Master Secret。服务按设备令牌单播，Android 带上 `channel_activity`，进程被杀掉后由华为、荣耀、小米、OPPO、vivo、魅族通道下发。`category` 固定为 1（服务与通讯）。iOS 开发证书保持 `production-mode: false`，生产证书改为 `true`。友盟后台还要关闭 IP 白名单，或把 push-server 的出口 IP 加进去。

客户端运行参数：

```text
--dart-define=UMENG_ANDROID_APPKEY=你的AndroidAppKey --dart-define=UMENG_IOS_APPKEY=你的iOSAppKey --dart-define=UMENG_MESSAGE_SECRET=你的MessageSecret
```

Android 厂商 AppId 写在 `flutter-demo/android/local.properties`，样例见 `android/umeng.properties.example`。小米、OPPO、vivo 的正式离线仍要求应用上架对应商店；华为、荣耀、魅族不要求上架。

## 接口

内部接口都要带请求头 `X-Internal-Token`。

| 方法 | 路径 | 调用方 | 作用 |
|------|------|--------|------|
| POST | `/internal/device` | app-server | 登记或覆盖一台设备 |
| DELETE | `/internal/device?userId=&deviceId=` | app-server | 退出登录时停用设备 |
| POST | `/internal/notify` | im-server | 对方不在线时请求通知 |

客户端不直连本服务。Flutter 拿 App Token 调用 app-server 的 `POST /api/client/push/device`。`platform` 为 `android` 或 `ios`，`provider` 为 `apns`、`hms`、`mipush`、`oppo`、`vivo`、`honor`、`fcm`、`jpush` 之一。

同一用户、同一 `bizType` + `bizId` 在两分钟内只投递一次。推送失败不影响消息已经发送成功。

一对一通话在对方不在线时仍直接结束，本服务不会把来电拉起来。那需要客户端能从通知栏回到通话页之后再做。
