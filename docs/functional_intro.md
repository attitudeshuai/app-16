# 通勤拼车便签板 - 功能说明

## 业务背景

同园区上班族每天通勤距离长、交通拥堵，许多人自驾但有空座。通过拼车可分摊油费、减少拥堵、结识同事。

## 用户角色

- 司机：发布路线、创建拼车行程、确认/拒绝预约
- 乘客：搜索路线、预约座位、评价拼车体验

## 核心用例

1. 司机发布固定通勤路线（起点、终点、时间、天数、座位数、单价）
2. 司机基于路线创建某天的拼车行程
3. 乘客搜索路线并预约拼车
4. 司机确认预约，系统自动扣减座位
5. 行程结束后双方互评
6. 查看统计看板（总行程数、热门路线、趋势）

## 数据库 ER 关系

- User 1--N Route（一个用户可拥有多条路线）
- Route 1--N Carpool（一条路线可产生多个行程）
- User(Driver) 1--N Carpool（一个司机可创建多个行程）
- Carpool 1--N CarpoolBooking（一个行程可有多个预约）
- User(Passenger) 1--N CarpoolBooking（一个乘客可有多个预约）
- Carpool 1--N CarpoolRating（一个行程可有多个评价）

## 业务规则

- 拼车状态流转：OPEN → FULL / COMPLETED / CANCELLED
- 预约状态流转：PENDING → CONFIRMED / CANCELLED
- 确认预约时自动扣减座位，座位为0时行程自动变为FULL
- 取消已确认预约时恢复座位，行程自动变为OPEN
- 只有路线/行程的创建者可以修改/删除
- 评分范围1-5，不能给自己评分

## 接口调用示例

1. 注册用户：
```
POST /api/auth/register
Body: {"username":"testuser","email":"test@example.com","password":"password123"}
```

2. 创建路线：
```
POST /api/routes
Headers: Authorization: Bearer <token>
Body: {"startLocation":"望京","endLocation":"中关村","startTime":"08:00","returnTime":"18:00","daysOfWeek":"MON,TUE,WED,THU,FRI","seats":4,"pricePerSeat":15.00}
```

3. 预约拼车：
```
POST /api/carpoolbookings
Headers: Authorization: Bearer <token>
Body: {"carpoolId":1,"seatsBooked":1}
```
