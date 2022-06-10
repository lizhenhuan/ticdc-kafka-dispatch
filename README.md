ticdc-kafka-dispatch介绍：
在 TiDB 6.1 之前 TiCDC 会将多个表数据发送到一个 Kafka Topic
此项目将 TiCDC 单个 Topic 的数据按照 db 和 table 做 topic 拆分。
例如 TiCDC 将数据发送到在 test_cdc topic 中原始数据如下：
{"id":0,"database":"bdp_admin","table":"","pkNames":null,"isDdl":true,"type":"QUERY","es":1652164375130,"ts":0,"sql":"create database bdp_admin","sqlType":null,"mysqlType":null,"data":null,"old":null}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":null,"isDdl":true,"type":"CREATE","es":1652164455179,"ts":0,"sql":"create table bdp_admin.bdp_job_node_run_history(id int primary key,type varchar(20),name varchar(20))","sqlType":null,"mysqlType":null,"data":null,"old":null}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652164487179,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"1","name":"工商银行A","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652165289929,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"2","name":"mert","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652166490029,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"3","name":"工商银行A","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"t1","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652254293229,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"100","name":"test","type":"a"}],"old":[null]}

运行dispatch项目之后会被拆分为两个 Topic：bdp_admin.bdp_job_node_run_history 和 bdp_admin.t1
bdp_admin.bdp_job_node_run_history topic数据如下：
{"id":0,"database":"bdp_admin","table":"","pkNames":null,"isDdl":true,"type":"QUERY","es":1652164375130,"ts":0,"sql":"create database bdp_admin","sqlType":null,"mysqlType":null,"data":null,"old":null}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":null,"isDdl":true,"type":"CREATE","es":1652164455179,"ts":0,"sql":"create table bdp_admin.bdp_job_node_run_history(id int primary key,type varchar(20),name varchar(20))","sqlType":null,"mysqlType":null,"data":null,"old":null}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652164487179,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"1","name":"工商银行A","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652165289929,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"2","name":"mert","type":"bank"}],"old":[null]}
{"id":0,"database":"bdp_admin","table":"bdp_job_node_run_history","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652166490029,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"3","name":"工商银行A","type":"bank"}],"old":[null]}

bdp_admin.t1 topic数据如下：
{"id":0,"database":"bdp_admin","table":"t1","pkNames":["id"],"isDdl":false,"type":"INSERT","es":1652254293229,"ts":0,"sql":"","sqlType":{"id":-5,"name":12,"type":12},"mysqlType":{"id":"int","name":"varchar","type":"varchar"},"data":[{"id":"100","name":"test","type":"a"}],"old":[null]}