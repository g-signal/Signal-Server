# Feature Specification: Signal-Server BAXS 绑定 REST 接口

**Feature Branch**: `001-gext-linkbapay-rest-api`
**Created**: 2026-05-14
**Status**: Draft

Signal-Server 暴露给 Signal 客户端的 BAXS 绑定相关 REST 接口规范。共 4 个端点，用于扫码、提交绑定、轮询结果、查询当前账号已绑定的 BA 操作员。

---

## 通用约定

### 路径前缀

所有端点统一前缀：`/v1/gext/linkbapay`

### 请求 / 响应格式

| 项 | 值 |
|---|---|
| HTTP 方法 | `POST` |
| `Content-Type` | `application/json` |
| `Accept` | `application/json` |
| 字符集 | UTF-8 |
| 日期格式 | ISO 8601 字符串，含时区，例如 `2026-05-14T10:30:00Z` |

### 鉴权

| 项 | 规则 |
|---|---|
| 认证方式 | Signal Basic Auth Header（与 `/v1/*` 其他接口一致） |
| 是否强制登录 | 是 |
| 未登录或 token 失效 | `401 Unauthorized` |

### 服务端自动填充的用户字段

以下字段由 **服务端从登录态自动填充**，客户端**不需要也不能传**，传了会被忽略：

| 字段 | 来源 |
|---|---|
| `baxsAppUserId` | 登录账号 ACI（UUID） |
| `baxsAppUserName` | 不填充（Signal 不持有用户明文显示名） |
| `baxsAppUserMobile` | 登录账号手机号（E.164） |
| `baxsAppUserEmail` | 不填充 |

### linkStatus 枚举

绑定流程状态码：

| Code | Name | 含义 |
|---|---|---|
| `1` | `PENDING_SCAN` | 二维码待扫描 |
| `2` | `SCANNED` | 已扫描、待平台确认 |
| `3` | `LINKED` | 已绑定成功 |
| `4` | `FAILED` | 绑定失败 / 被拒绝 |
| `5` | `TIMEOUT` | 绑定超时（终态） |

### 错误码

| HTTP 状态 | 含义 | 客户端处理建议 |
|---|---|---|
| `200` | 成功 | 解析响应体 |
| `401` | 未登录、token 失效或账号不存在 | 跳登录页 |
| `502` | 后端不可达或返回业务错误 | 提示稍后重试 |

---

## API 1：查询 BA 用户绑定信息

**Endpoint**：`POST /v1/gext/linkbapay/link/getBaUserInfo`

**Purpose**：客户端扫描二维码后，查询二维码对应的 BA 商户/操作员展示信息，并判断当前是否允许继续提交绑定申请。

### Request

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `linkId` | String | 是 | 二维码 ID（扫描结果 `baxs://linkba?linkId={linkId}` 中的 `linkId`） |

```json
{
  "linkId": "link-001"
}
```

### Response（HTTP 200）

| 字段 | 类型 | 说明 |
|---|---|---|
| `linkId` | String | 回显的二维码 ID |
| `optId` | String | BA 操作员 ID |
| `memberId` | String | BA 商户 ID |
| `optName` | String | 操作员姓名 |
| `email` | String | 操作员邮箱（可脱敏） |
| `mobile` | String | 操作员手机号（可脱敏） |
| `linkStatus` | Integer | 当前状态码 1/2/3/4/5 |
| `linkStatusName` | String | 当前状态名 |
| `canRequestLink` | Boolean | 是否允许接下来调用 `requestLink` |
| `failReason` | String | `canRequestLink=false` 时给出原因；否则为空 |
| `expireTime` | DateTime | 二维码 / 绑定申请的过期时间 |

```json
{
  "linkId": "link-001",
  "optId": "opt-1",
  "memberId": "mem-1",
  "optName": "Alice",
  "email": "a***@example.com",
  "mobile": "138****0000",
  "linkStatus": 1,
  "linkStatusName": "PENDING_SCAN",
  "canRequestLink": true,
  "failReason": null,
  "expireTime": "2026-05-14T11:00:00Z"
}
```

### linkStatus 情形

| 状态 | 含义 | 客户端可继续操作 |
|---|---|---|
| `1 PENDING_SCAN` | 二维码有效、未提交申请 | 可继续 `requestLink` |
| `2 SCANNED` | 已提交申请，等待平台确认 | 改为 `getLinkResult` 轮询 |
| `3 LINKED` | 已绑定成功 | 不可重复绑定 |
| `4 FAILED` | 已拒绝 / 失败，附 `failReason` | 不可继续 |
| `5 TIMEOUT` | 已过期 | 不可继续 |

---

## API 2：提交绑定申请

**Endpoint**：`POST /v1/gext/linkbapay/link/requestLink`

**Purpose**：以当前登录账号身份提交绑定申请。

### Request

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `linkId` | String | 是 | 二维码 ID |

```json
{
  "linkId": "link-001"
}
```

### Response（HTTP 200）

| 字段 | 类型 | 说明 |
|---|---|---|
| `linkId` | String | 回显的二维码 ID |
| `optId` | String | 关联的 BA 操作员 ID |
| `memberId` | String | 关联的商户 ID |
| `baxsAppUserId` | String | 已写入的 app 用户 ID（= 当前账号 UUID） |
| `linkStatus` | Integer | 固定 `2 SCANNED` |
| `linkStatusName` | String | 固定 `SCANNED` |
| `expireTime` | DateTime | 申请过期时间 |

```json
{
  "linkId": "link-001",
  "optId": "opt-1",
  "memberId": "mem-1",
  "baxsAppUserId": "11111111-2222-3333-4444-555555555555",
  "linkStatus": 2,
  "linkStatusName": "SCANNED",
  "expireTime": "2026-05-14T11:00:00Z"
}
```

### 幂等

同一 `baxsAppUserId + linkId` 重复提交：返回原 `linkId` 与 `SCANNED`，不创建新绑定记录。

---

## API 3：查询绑定结果

**Endpoint**：`POST /v1/gext/linkbapay/link/getLinkResult`

**Purpose**：轮询绑定申请的最终结果。

### Request

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `linkId` | String | 是 | 二维码 ID |

```json
{
  "linkId": "link-001"
}
```

### Response（HTTP 200）

| 字段 | 类型 | 说明 |
|---|---|---|
| `linkId` | String | 回显的二维码 ID |
| `optId` | String | 操作员 ID |
| `memberId` | String | 商户 ID |
| `baxsAppUserId` | String | App 用户 ID |
| `linkStatus` | Integer | 当前状态码 1/2/3/4/5 |
| `linkStatusName` | String | 当前状态名 |
| `confirmTime` | DateTime | 确认时间，仅 `linkStatus=3` 时有值 |
| `failReason` | String | 失败 / 超时原因，`linkStatus=4/5` 时给出 |

```json
{
  "linkId": "link-001",
  "optId": "opt-1",
  "memberId": "mem-1",
  "baxsAppUserId": "11111111-2222-3333-4444-555555555555",
  "linkStatus": 3,
  "linkStatusName": "LINKED",
  "confirmTime": "2026-05-14T10:30:00Z",
  "failReason": null
}
```

### 轮询建议

- 客户端 2–5 秒一次轮询
- 命中 `3 / 4 / 5` 任一终态即停止轮询
- 超过本地 60 秒兜底退出，提示用户绑定超时

---

## API 4：查询当前账号已绑定的 BA 操作员

**Endpoint**：`POST /v1/gext/linkbapay/link/getLinkedBaUserInfo`

**Purpose**：在客户端进入 BA 相关入口时，查询当前登录账号是否已经绑定过 BA 操作员，若已绑定则回显操作员展示信息。数据来自 ext-tag 本地的 `t_signal_user` 表（在 `getLinkResult` 收到 `LINKED` 终态时落库）。

### Request

无请求体。`baxsAppUserId` 由服务端从登录态派生（见「服务端自动填充的用户字段」），客户端不需要也不能传。

```http
POST /v1/gext/linkbapay/link/getLinkedBaUserInfo
Content-Type: application/json

(空 body)
```

### Response（HTTP 200）

| 字段 | 类型 | 说明 |
|---|---|---|
| `baxsAppUserId` | String | 回显的 app 用户 ID（= 当前账号 UUID） |
| `linkbaxsOptId` | String | 已绑定的 BA 操作员 ID；未绑定时为 `null` |
| `linkbaxsMemberId` | String | 已绑定的 BA 商户 ID；未绑定时为 `null` |
| `linkbaxsOptName` | String | 操作员姓名；未绑定时为 `null` |
| `linkbaxsOptEmail` | String | 操作员邮箱；未绑定时为 `null` |
| `linkbaxsOptMobile` | String | 操作员手机号；未绑定时为 `null` |
| `linkbaxsDate` | DateTime | 绑定完成时间；未绑定时为 `null` |

已绑定示例：

```json
{
  "baxsAppUserId": "11111111-2222-3333-4444-555555555555",
  "linkbaxsOptId": "opt-1",
  "linkbaxsMemberId": "mem-1",
  "linkbaxsOptName": "Alice",
  "linkbaxsOptEmail": "alice@example.com",
  "linkbaxsOptMobile": "+8613800000000",
  "linkbaxsDate": "2026-05-14T10:30:00Z"
}
```

未绑定示例：

```json
{
  "baxsAppUserId": "11111111-2222-3333-4444-555555555555",
  "linkbaxsOptId": null,
  "linkbaxsMemberId": null,
  "linkbaxsOptName": null,
  "linkbaxsOptEmail": null,
  "linkbaxsOptMobile": null,
  "linkbaxsDate": null
}
```

### 客户端处理建议

- `linkbaxsOptId == null` → 当前账号未绑定，引导用户走扫码绑定流程（API 1 → API 2 → API 3）
- `linkbaxsOptId != null` → 已绑定，直接展示绑定信息；如需重新绑定走业务侧解绑流程，本接口不负责
