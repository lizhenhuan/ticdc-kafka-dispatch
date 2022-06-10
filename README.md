ticdc-kafka-dispatch介绍：

在 TiDB 6.1 之前 TiCDC 会将多个表数据发送到一个 Kafka Topic
此项目将 TiCDC 单个 Topic 的数据按照 db 和 table 做 topic 拆分。
例如 TiCDC 将数据发送到在 test_cdc topic 中原始数据如下：
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652166490029,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"3","name":"工商银行A","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"t1","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652254293229,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"100","name":"test","type":"a"}],"old":[null]}

运行dispatch项目之后会被拆分为两个 Topic：bdp_admin.bdp_job_node_run_history 和 bdp_admin.t1
bdp_admin.bdp_job_node_run_history topic数据如下：
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652166490029,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"3","name":"工商银行A","type":"bank"}],"old":[null]}

bdp_admin.t1 topic数据如下：
{"id":0,"database":"bdp_admin","table":"t1","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652254293229,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"100","name":"test","type":"a"}],"old":[null]}


项目使用介绍：

项目编译：

mvn clean package

项目启动：

java -cp target/ticdc-kafka-dispatch-1.0-SNAPSHOT.jar com.pingcap.ticdc.dispatch.Dispatch bootstrap.servers topic groupId