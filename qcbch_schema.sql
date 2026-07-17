/*
 Navicat Premium Data Transfer

 Source Server         : qcbch
 Source Server Type    : PostgreSQL
 Source Server Version : 160014 (160014)
 Source Host           : 10.168.89.200:5432
 Source Catalog        : postgres
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160014 (160014)
 File Encoding         : 65001

 Date: 15/07/2026 17:08:59
*/


-- ----------------------------
-- Type structure for box2d
-- ----------------------------
DROP TYPE IF EXISTS "public"."box2d";
CREATE TYPE "public"."box2d" (
  INPUT = "public"."box2d_in",
  OUTPUT = "public"."box2d_out",
  INTERNALLENGTH = 65,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."box2d" OWNER TO "postgres";

-- ----------------------------
-- Type structure for box2df
-- ----------------------------
DROP TYPE IF EXISTS "public"."box2df";
CREATE TYPE "public"."box2df" (
  INPUT = "public"."box2df_in",
  OUTPUT = "public"."box2df_out",
  INTERNALLENGTH = 16,
  ALIGNMENT = double,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."box2df" OWNER TO "postgres";

-- ----------------------------
-- Type structure for box3d
-- ----------------------------
DROP TYPE IF EXISTS "public"."box3d";
CREATE TYPE "public"."box3d" (
  INPUT = "public"."box3d_in",
  OUTPUT = "public"."box3d_out",
  INTERNALLENGTH = 52,
  ALIGNMENT = double,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."box3d" OWNER TO "postgres";

-- ----------------------------
-- Type structure for geography
-- ----------------------------
DROP TYPE IF EXISTS "public"."geography";
CREATE TYPE "public"."geography" (
  INPUT = "public"."geography_in",
  OUTPUT = "public"."geography_out",
  RECEIVE = "public"."geography_recv",
  SEND = "public"."geography_send",
  TYPMOD_IN = "public"."geography_typmod_in",
  TYPMOD_OUT = "public"."geography_typmod_out",
  ANALYZE = "public"."geography_analyze",
  INTERNALLENGTH = VARIABLE,
  ALIGNMENT = double,
  STORAGE = main,
  CATEGORY = U,
  DELIMITER = ':'
);
ALTER TYPE "public"."geography" OWNER TO "postgres";

-- ----------------------------
-- Type structure for geometry
-- ----------------------------
DROP TYPE IF EXISTS "public"."geometry";
CREATE TYPE "public"."geometry" (
  INPUT = "public"."geometry_in",
  OUTPUT = "public"."geometry_out",
  RECEIVE = "public"."geometry_recv",
  SEND = "public"."geometry_send",
  TYPMOD_IN = "public"."geometry_typmod_in",
  TYPMOD_OUT = "public"."geometry_typmod_out",
  ANALYZE = "public"."geometry_analyze",
  INTERNALLENGTH = VARIABLE,
  ALIGNMENT = double,
  STORAGE = main,
  CATEGORY = U,
  DELIMITER = ':'
);
ALTER TYPE "public"."geometry" OWNER TO "postgres";

-- ----------------------------
-- Type structure for geometry_dump
-- ----------------------------
DROP TYPE IF EXISTS "public"."geometry_dump";
CREATE TYPE "public"."geometry_dump" AS (
  "path" int4[],
  "geom" "public"."geometry"
);
ALTER TYPE "public"."geometry_dump" OWNER TO "postgres";

-- ----------------------------
-- Type structure for gidx
-- ----------------------------
DROP TYPE IF EXISTS "public"."gidx";
CREATE TYPE "public"."gidx" (
  INPUT = "public"."gidx_in",
  OUTPUT = "public"."gidx_out",
  INTERNALLENGTH = VARIABLE,
  ALIGNMENT = double,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."gidx" OWNER TO "postgres";

-- ----------------------------
-- Type structure for halfvec
-- ----------------------------
DROP TYPE IF EXISTS "public"."halfvec";
CREATE TYPE "public"."halfvec" (
  INPUT = "public"."halfvec_in",
  OUTPUT = "public"."halfvec_out",
  RECEIVE = "public"."halfvec_recv",
  SEND = "public"."halfvec_send",
  TYPMOD_IN = "public"."halfvec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."halfvec" OWNER TO "postgres";

-- ----------------------------
-- Type structure for sparsevec
-- ----------------------------
DROP TYPE IF EXISTS "public"."sparsevec";
CREATE TYPE "public"."sparsevec" (
  INPUT = "public"."sparsevec_in",
  OUTPUT = "public"."sparsevec_out",
  RECEIVE = "public"."sparsevec_recv",
  SEND = "public"."sparsevec_send",
  TYPMOD_IN = "public"."sparsevec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."sparsevec" OWNER TO "postgres";

-- ----------------------------
-- Type structure for spheroid
-- ----------------------------
DROP TYPE IF EXISTS "public"."spheroid";
CREATE TYPE "public"."spheroid" (
  INPUT = "public"."spheroid_in",
  OUTPUT = "public"."spheroid_out",
  INTERNALLENGTH = 65,
  ALIGNMENT = double,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."spheroid" OWNER TO "postgres";

-- ----------------------------
-- Type structure for valid_detail
-- ----------------------------
DROP TYPE IF EXISTS "public"."valid_detail";
CREATE TYPE "public"."valid_detail" AS (
  "valid" bool,
  "reason" varchar COLLATE "pg_catalog"."default",
  "location" "public"."geometry"
);
ALTER TYPE "public"."valid_detail" OWNER TO "postgres";

-- ----------------------------
-- Type structure for vector
-- ----------------------------
DROP TYPE IF EXISTS "public"."vector";
CREATE TYPE "public"."vector" (
  INPUT = "public"."vector_in",
  OUTPUT = "public"."vector_out",
  RECEIVE = "public"."vector_recv",
  SEND = "public"."vector_send",
  TYPMOD_IN = "public"."vector_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."vector" OWNER TO "postgres";

-- ----------------------------
-- Sequence structure for alert_message_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."alert_message_id_seq";
CREATE SEQUENCE "public"."alert_message_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for alert_rule_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."alert_rule_id_seq";
CREATE SEQUENCE "public"."alert_rule_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for device_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."device_id_seq";
CREATE SEQUENCE "public"."device_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for disease_catalog_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."disease_catalog_id_seq";
CREATE SEQUENCE "public"."disease_catalog_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for disease_detection_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."disease_detection_id_seq";
CREATE SEQUENCE "public"."disease_detection_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for environment_data_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."environment_data_id_seq";
CREATE SEQUENCE "public"."environment_data_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for farming_calendar_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."farming_calendar_id_seq";
CREATE SEQUENCE "public"."farming_calendar_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for firmware_package_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."firmware_package_id_seq";
CREATE SEQUENCE "public"."firmware_package_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for iotda_device_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."iotda_device_id_seq";
CREATE SEQUENCE "public"."iotda_device_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for knowledge_chunk_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."knowledge_chunk_id_seq";
CREATE SEQUENCE "public"."knowledge_chunk_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for knowledge_document_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."knowledge_document_id_seq";
CREATE SEQUENCE "public"."knowledge_document_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for orchard_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."orchard_id_seq";
CREATE SEQUENCE "public"."orchard_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for rag_feedback_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."rag_feedback_id_seq";
CREATE SEQUENCE "public"."rag_feedback_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for risk_prediction_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."risk_prediction_id_seq";
CREATE SEQUENCE "public"."risk_prediction_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_operation_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_operation_log_id_seq";
CREATE SEQUENCE "public"."sys_operation_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_refresh_token_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_refresh_token_id_seq";
CREATE SEQUENCE "public"."sys_refresh_token_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_role_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_role_id_seq";
CREATE SEQUENCE "public"."sys_role_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_id_seq";
CREATE SEQUENCE "public"."sys_user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for user_feedback_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."user_feedback_id_seq";
CREATE SEQUENCE "public"."user_feedback_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for weather_forecast_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."weather_forecast_id_seq";
CREATE SEQUENCE "public"."weather_forecast_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for alert_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."alert_message";
CREATE TABLE "public"."alert_message" (
  "id" int8 NOT NULL DEFAULT nextval('alert_message_id_seq'::regclass),
  "orchard_id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "rule_id" int4,
  "calendar_id" int4,
  "message_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'alert'::character varying,
  "level" int2 NOT NULL,
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "summary" varchar(500) COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "advice" text COLLATE "pg_catalog"."default",
  "related_type" varchar(30) COLLATE "pg_catalog"."default",
  "related_id" int8,
  "extra_data" jsonb,
  "push_channel" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'app'::character varying,
  "push_status" int2 NOT NULL DEFAULT 0,
  "push_time" timestamp(6),
  "read_status" int2 NOT NULL DEFAULT 0,
  "read_time" timestamp(6),
  "processed" bool DEFAULT false,
  "treatment_images" jsonb,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."alert_message"."message_type" IS 'alert=预警消息 / calendar=农事提醒 / system=系统通知';
COMMENT ON COLUMN "public"."alert_message"."level" IS '1=L1 低风险-关注 / 2=L2 中风险-预警 / 3=L3 高风险-紧急';
COMMENT ON COLUMN "public"."alert_message"."push_channel" IS 'app=应用内消息 / sms=短信';
COMMENT ON TABLE "public"."alert_message" IS '预警消息表';

-- ----------------------------
-- Table structure for alert_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."alert_rule";
CREATE TABLE "public"."alert_rule" (
  "id" int4 NOT NULL DEFAULT nextval('alert_rule_id_seq'::regclass),
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "level" int2 NOT NULL,
  "description" varchar(500) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "advice_template" text COLLATE "pg_catalog"."default",
  "temp_min" numeric(5,2),
  "temp_max" numeric(5,2),
  "humidity_min" numeric(5,2),
  "humidity_max" numeric(5,2),
  "disease_code" varchar(20) COLLATE "pg_catalog"."default",
  "detection_count" int4,
  "detection_days" int4,
  "forecast_rain_min" numeric(8,2),
  "forecast_wind_min" numeric(5,2),
  "enabled" bool NOT NULL DEFAULT true,
  "cooldown_hours" int4 NOT NULL DEFAULT 24,
  "created_by" int8,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "public"."alert_rule" IS '预警规则表';

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS "public"."device";
CREATE TABLE "public"."device" (
  "id" int8 NOT NULL DEFAULT nextval('device_id_seq'::regclass),
  "device_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "orchard_id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "type" varchar(30) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'camera'::character varying,
  "model" varchar(50) COLLATE "pg_catalog"."default",
  "api_key" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "location_desc" varchar(100) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "last_heartbeat" timestamp(6),
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."device"."device_code" IS '设备编码（硬件烧录）';
COMMENT ON COLUMN "public"."device"."type" IS '设备类型：camera=摄像头 sensor=传感器';
COMMENT ON COLUMN "public"."device"."api_key" IS '设备API密钥（SHA256存储）';
COMMENT ON COLUMN "public"."device"."status" IS '状态：0=离线 1=在线 2=故障';
COMMENT ON TABLE "public"."device" IS '边缘设备表';

-- ----------------------------
-- Table structure for disease_catalog
-- ----------------------------
DROP TABLE IF EXISTS "public"."disease_catalog";
CREATE TABLE "public"."disease_catalog" (
  "id" int4 NOT NULL DEFAULT nextval('disease_catalog_id_seq'::regclass),
  "disease_code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "disease_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "disease_name_en" varchar(100) COLLATE "pg_catalog"."default",
  "disease_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "category" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "severity_level" int2 NOT NULL DEFAULT 1,
  "symptom_desc" text COLLATE "pg_catalog"."default",
  "prevention_guide" text COLLATE "pg_catalog"."default",
  "symptom_quiz_template" jsonb,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "public"."disease_catalog" IS '病害字典表';

-- ----------------------------
-- Table structure for disease_detection
-- ----------------------------
DROP TABLE IF EXISTS "public"."disease_detection";
CREATE TABLE "public"."disease_detection" (
  "id" int8 NOT NULL DEFAULT nextval('disease_detection_id_seq'::regclass),
  "device_id" int8 NOT NULL,
  "orchard_id" int8 NOT NULL,
  "disease_catalog_id" int4 NOT NULL,
  "disease_name_raw" varchar(50) COLLATE "pg_catalog"."default",
  "top_class_code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "top_probability" numeric(5,4) NOT NULL,
  "all_probabilities" jsonb,
  "image_url" varchar(500) COLLATE "pg_catalog"."default",
  "device_timestamp" timestamp(6) NOT NULL,
  "extra_data" jsonb,
  "processed" bool DEFAULT false,
  "processed_at" timestamp(6),
  "treatment_images" jsonb,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "public"."disease_detection" IS '病害识别记录表';

-- ----------------------------
-- Table structure for environment_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."environment_data";
CREATE TABLE "public"."environment_data" (
  "id" int8 NOT NULL DEFAULT nextval('environment_data_id_seq'::regclass),
  "orchard_id" int8 NOT NULL,
  "data_source" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'sensor'::character varying,
  "temp" numeric(5,2),
  "humidity" numeric(5,2),
  "precip" numeric(8,2),
  "windspeed" numeric(5,2),
  "record_time" timestamp(6) NOT NULL,
  "extra_data" jsonb,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."environment_data"."data_source" IS '数据来源：sensor=传感器/weather_api=天气API';
COMMENT ON COLUMN "public"."environment_data"."temp" IS '温度（℃）（对应和风天气API temp字段）';
COMMENT ON COLUMN "public"."environment_data"."humidity" IS '相对湿度（%）（对应和风天气API humidity字段）';
COMMENT ON COLUMN "public"."environment_data"."precip" IS '降雨量（mm）（对应和风天气API precip字段）';
COMMENT ON COLUMN "public"."environment_data"."windspeed" IS '风速（km/h）（对应和风天气API windSpeed字段）';
COMMENT ON TABLE "public"."environment_data" IS '环境数据表';

-- ----------------------------
-- Table structure for farming_calendar
-- ----------------------------
DROP TABLE IF EXISTS "public"."farming_calendar";
CREATE TABLE "public"."farming_calendar" (
  "id" int4 NOT NULL DEFAULT nextval('farming_calendar_id_seq'::regclass),
  "orchard_id" int8,
  "phenology_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "solar_term" varchar(20) COLLATE "pg_catalog"."default",
  "start_month" int2 NOT NULL,
  "start_day" int2 NOT NULL,
  "end_month" int2 NOT NULL,
  "end_day" int2 NOT NULL,
  "advice_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "disease_related" jsonb,
  "sort_order" int4 NOT NULL DEFAULT 0,
  "enabled" bool NOT NULL DEFAULT true,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."farming_calendar"."orchard_id" IS 'NULL=全局通用，指定=某果园专属';
COMMENT ON COLUMN "public"."farming_calendar"."phenology_name" IS '物候期名称';
COMMENT ON COLUMN "public"."farming_calendar"."solar_term" IS '关联节气';
COMMENT ON COLUMN "public"."farming_calendar"."disease_related" IS '关联病害编码数组：["S14","S10"]';
COMMENT ON TABLE "public"."farming_calendar" IS '农事日历表';

-- ----------------------------
-- Table structure for firmware_package
-- ----------------------------
DROP TABLE IF EXISTS "public"."firmware_package";
CREATE TABLE "public"."firmware_package" (
  "id" int8 NOT NULL DEFAULT nextval('firmware_package_id_seq'::regclass),
  "file_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "version" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "file_size" int8 NOT NULL DEFAULT 0,
  "file_url" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "md5" varchar(64) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 0,
  "ota_product_id" varchar(128) COLLATE "pg_catalog"."default",
  "ota_package_id" varchar(128) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."firmware_package"."file_name" IS '文件名（如 test_01.bin）';
COMMENT ON COLUMN "public"."firmware_package"."version" IS '版本号（如 v1.0.1）';
COMMENT ON COLUMN "public"."firmware_package"."status" IS '状态: 0=待发布 1=已发布 2=已废弃';
COMMENT ON COLUMN "public"."firmware_package"."ota_product_id" IS '华为云 IoTDA 产品 ID';
COMMENT ON COLUMN "public"."firmware_package"."ota_package_id" IS '华为云 IoTDA 升级包 ID';
COMMENT ON TABLE "public"."firmware_package" IS '固件包管理';

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."flyway_schema_history";
CREATE TABLE "public"."flyway_schema_history" (
  "installed_rank" int4 NOT NULL,
  "version" varchar(50) COLLATE "pg_catalog"."default",
  "description" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "script" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "checksum" int4,
  "installed_by" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "installed_on" timestamp(6) NOT NULL DEFAULT now(),
  "execution_time" int4 NOT NULL,
  "success" bool NOT NULL
)
;

-- ----------------------------
-- Table structure for iotda_device
-- ----------------------------
DROP TABLE IF EXISTS "public"."iotda_device";
CREATE TABLE "public"."iotda_device" (
  "id" int8 NOT NULL DEFAULT nextval('iotda_device_id_seq'::regclass),
  "device_id" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "device_name" varchar(200) COLLATE "pg_catalog"."default",
  "product_id" varchar(128) COLLATE "pg_catalog"."default",
  "product_name" varchar(200) COLLATE "pg_catalog"."default",
  "status" varchar(20) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "fw_version" varchar(50) COLLATE "pg_catalog"."default",
  "sw_version" varchar(50) COLLATE "pg_catalog"."default",
  "local_device_id" int8,
  "sync_time" timestamp(6),
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."iotda_device"."device_id" IS 'IoTDA设备ID';
COMMENT ON COLUMN "public"."iotda_device"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."iotda_device"."product_id" IS '产品ID';
COMMENT ON COLUMN "public"."iotda_device"."product_name" IS '产品名称';
COMMENT ON COLUMN "public"."iotda_device"."status" IS '设备状态：ONLINE, OFFLINE, INACTIVE';
COMMENT ON COLUMN "public"."iotda_device"."local_device_id" IS '关联的本地设备ID（device表的id）';
COMMENT ON COLUMN "public"."iotda_device"."sync_time" IS '同步时间';
COMMENT ON TABLE "public"."iotda_device" IS 'IoTDA设备信息表';

-- ----------------------------
-- Table structure for knowledge_chunk
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_chunk";
CREATE TABLE "public"."knowledge_chunk" (
  "id" int8 NOT NULL DEFAULT nextval('knowledge_chunk_id_seq'::regclass),
  "document_id" int8 NOT NULL,
  "chunk_index" int4 NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "content_hash" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "embedding" "public"."vector",
  "metadata" jsonb,
  "token_count" int4,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."knowledge_chunk"."embedding" IS '512维文本嵌入向量';
COMMENT ON TABLE "public"."knowledge_chunk" IS '知识分块表（使用 pgvector 向量类型）';

-- ----------------------------
-- Table structure for knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_document";
CREATE TABLE "public"."knowledge_document" (
  "id" int8 NOT NULL DEFAULT nextval('knowledge_document_id_seq'::regclass),
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "file_url" varchar(500) COLLATE "pg_catalog"."default",
  "file_type" varchar(20) COLLATE "pg_catalog"."default",
  "file_size" int8,
  "content_text" text COLLATE "pg_catalog"."default",
  "category" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "source" varchar(30) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'ADMIN_UPLOAD'::character varying,
  "source_id" int8,
  "chunk_count" int4 NOT NULL DEFAULT 0,
  "embedding_model" varchar(50) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 0,
  "review_status" int2 NOT NULL DEFAULT 0,
  "review_comment" varchar(500) COLLATE "pg_catalog"."default",
  "reviewed_by" int8,
  "reviewed_at" timestamp(6),
  "error_message" text COLLATE "pg_catalog"."default",
  "uploaded_by" int8,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."knowledge_document"."source" IS '来源：ADMIN_UPLOAD=管理员上传 / RAG_FLOW=自动采集';
COMMENT ON COLUMN "public"."knowledge_document"."status" IS '处理状态：0=待处理 1=处理中 2=已完成 3=失败';
COMMENT ON COLUMN "public"."knowledge_document"."review_status" IS '审核状态：0=待审核 1=通过 2=驳回';
COMMENT ON TABLE "public"."knowledge_document" IS '知识文献表';

-- ----------------------------
-- Table structure for orchard
-- ----------------------------
DROP TABLE IF EXISTS "public"."orchard";
CREATE TABLE "public"."orchard" (
  "id" int8 NOT NULL DEFAULT nextval('orchard_id_seq'::regclass),
  "owner_id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "formatted_address" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "location" geography(POINT, 4326),
  "area_mu" numeric(10,2),
  "tree_age" int4,
  "variety" varchar(50) COLLATE "pg_catalog"."default" DEFAULT '脐橙'::character varying,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "public"."orchard"."owner_id" IS '园主ID（主要负责人）';
COMMENT ON COLUMN "public"."orchard"."formatted_address" IS '详细地址（高德API地理编码后的完整地址）';
COMMENT ON COLUMN "public"."orchard"."location" IS '经纬度，格式：经度,纬度';
COMMENT ON COLUMN "public"."orchard"."status" IS '状态：0=停用 1=正常';
COMMENT ON TABLE "public"."orchard" IS '果园表';

-- ----------------------------
-- Table structure for orchard_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."orchard_user";
CREATE TABLE "public"."orchard_user" (
  "orchard_id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "role_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'viewer'::character varying,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."orchard_user"."role_type" IS '角色：owner=园主/manager=管理员/viewer=只读';
COMMENT ON TABLE "public"."orchard_user" IS '果园用户关联表';

-- ----------------------------
-- Table structure for rag_feedback
-- ----------------------------
DROP TABLE IF EXISTS "public"."rag_feedback";
CREATE TABLE "public"."rag_feedback" (
  "id" int8 NOT NULL DEFAULT nextval('rag_feedback_id_seq'::regclass),
  "session_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "question" text COLLATE "pg_catalog"."default" NOT NULL,
  "answer" text COLLATE "pg_catalog"."default" NOT NULL,
  "model" varchar(100) COLLATE "pg_catalog"."default",
  "similarity" numeric(5,4),
  "feedback_type" int2 NOT NULL DEFAULT 0,
  "user_id" int8,
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."rag_feedback"."feedback_type" IS '反馈类型：0=无反馈 1=有用 2=无用 3=不准确';
COMMENT ON TABLE "public"."rag_feedback" IS 'RAG问答反馈表';

-- ----------------------------
-- Table structure for risk_prediction
-- ----------------------------
DROP TABLE IF EXISTS "public"."risk_prediction";
CREATE TABLE "public"."risk_prediction" (
  "id" int8 NOT NULL DEFAULT nextval('risk_prediction_id_seq'::regclass),
  "orchard_id" int8 NOT NULL,
  "risk_level" int2 NOT NULL DEFAULT 0,
  "risk_score" numeric(5,2),
  "probabilities" jsonb,
  "llm_analysis" text COLLATE "pg_catalog"."default",
  "suggestions" text COLLATE "pg_catalog"."default",
  "top_factors" jsonb,
  "trigger_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'scheduled'::character varying,
  "trigger_detail" varchar(200) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."risk_prediction"."risk_level" IS '风险等级：0=低 1=中 2=高 3=严重';
COMMENT ON COLUMN "public"."risk_prediction"."trigger_type" IS '触发方式：scheduled=定时 / manual=手动';
COMMENT ON TABLE "public"."risk_prediction" IS '风险预测结果表';

-- ----------------------------
-- Table structure for spatial_ref_sys
-- ----------------------------
DROP TABLE IF EXISTS "public"."spatial_ref_sys";
CREATE TABLE "public"."spatial_ref_sys" (
  "srid" int4 NOT NULL,
  "auth_name" varchar(256) COLLATE "pg_catalog"."default",
  "auth_srid" int4,
  "srtext" varchar(2048) COLLATE "pg_catalog"."default",
  "proj4text" varchar(2048) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_operation_log";
CREATE TABLE "public"."sys_operation_log" (
  "id" int8 NOT NULL DEFAULT nextval('sys_operation_log_id_seq'::regclass),
  "user_id" int8,
  "module" varchar(30) COLLATE "pg_catalog"."default",
  "action" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "resource_type" varchar(50) COLLATE "pg_catalog"."default",
  "resource_id" varchar(50) COLLATE "pg_catalog"."default",
  "description" varchar(200) COLLATE "pg_catalog"."default",
  "detail" text COLLATE "pg_catalog"."default",
  "ip_address" varchar(45) COLLATE "pg_catalog"."default",
  "user_agent" varchar(500) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."sys_operation_log"."module" IS '操作模块：auth/user/orchard/device/knowledge/feedback';
COMMENT ON COLUMN "public"."sys_operation_log"."action" IS '操作类型：login/logout/create/update/delete/export';
COMMENT ON TABLE "public"."sys_operation_log" IS '操作日志表';

-- ----------------------------
-- Table structure for sys_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_refresh_token";
CREATE TABLE "public"."sys_refresh_token" (
  "id" int8 NOT NULL DEFAULT nextval('sys_refresh_token_id_seq'::regclass),
  "token_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" int8 NOT NULL,
  "token_hash" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "device_info" varchar(200) COLLATE "pg_catalog"."default",
  "expires_at" timestamp(6) NOT NULL,
  "revoked" bool NOT NULL DEFAULT false,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."sys_refresh_token"."token_id" IS '刷新令牌标识ID（返回给前端）';
COMMENT ON COLUMN "public"."sys_refresh_token"."token_hash" IS 'RefreshToken的SHA256哈希';
COMMENT ON TABLE "public"."sys_refresh_token" IS 'Refresh Token备份表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role";
CREATE TABLE "public"."sys_role" (
  "id" int4 NOT NULL DEFAULT nextval('sys_role_id_seq'::regclass),
  "code" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "public"."sys_role" IS '角色表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user";
CREATE TABLE "public"."sys_user" (
  "id" int8 NOT NULL DEFAULT nextval('sys_user_id_seq'::regclass),
  "username" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "phone" varchar(20) COLLATE "pg_catalog"."default",
  "wechat_openid" varchar(64) COLLATE "pg_catalog"."default",
  "avatar_url" varchar(500) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "email_enabled" bool NOT NULL DEFAULT false,
  "push_enabled" bool NOT NULL DEFAULT true,
  "email" varchar(100) COLLATE "pg_catalog"."default",
  "last_login_at" timestamp(6),
  "last_login_ip" varchar(45) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "public"."sys_user"."status" IS '状态：0=禁用 1=启用';
COMMENT ON COLUMN "public"."sys_user"."email_enabled" IS '是否接收邮件推送';
COMMENT ON COLUMN "public"."sys_user"."push_enabled" IS '是否接收小程序推送';
COMMENT ON COLUMN "public"."sys_user"."deleted" IS '逻辑删除标记';
COMMENT ON TABLE "public"."sys_user" IS '用户表';

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_role";
CREATE TABLE "public"."sys_user_role" (
  "user_id" int8 NOT NULL,
  "role_id" int4 NOT NULL
)
;
COMMENT ON TABLE "public"."sys_user_role" IS '用户角色关联表';

-- ----------------------------
-- Table structure for user_feedback
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_feedback";
CREATE TABLE "public"."user_feedback" (
  "id" int8 NOT NULL DEFAULT nextval('user_feedback_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "orchard_id" int8 NOT NULL,
  "image_urls" jsonb,
  "description" text COLLATE "pg_catalog"."default",
  "feedback_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ai_diagnosis" jsonb,
  "ai_top_disease" varchar(50) COLLATE "pg_catalog"."default",
  "ai_confidence" numeric(5,4),
  "symptom_quiz" jsonb,
  "user_answers" jsonb,
  "match_score" numeric(3,2),
  "review_status" int2 NOT NULL DEFAULT 0,
  "reviewed_by" int8,
  "reviewed_at" timestamp(6),
  "review_comment" varchar(500) COLLATE "pg_catalog"."default",
  "knowledge_doc_id" int8,
  "dataset_tag" varchar(50) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."user_feedback"."feedback_type" IS '反馈类型：disease=病害识别 / growth=生长情况 / pest=虫害 / other=其他';
COMMENT ON COLUMN "public"."user_feedback"."review_status" IS '审核状态：0=待审核 1=已采纳 2=已驳回';
COMMENT ON TABLE "public"."user_feedback" IS '用户反馈表';

-- ----------------------------
-- Table structure for weather_forecast
-- ----------------------------
DROP TABLE IF EXISTS "public"."weather_forecast";
CREATE TABLE "public"."weather_forecast" (
  "id" int8 NOT NULL DEFAULT nextval('weather_forecast_id_seq'::regclass),
  "orchard_id" int8 NOT NULL,
  "fxtime" timestamp(6) NOT NULL,
  "temp_max" numeric(5,2),
  "temp_min" numeric(5,2),
  "humidity" numeric(5,2),
  "pop" numeric(5,2),
  "precip" numeric(8,2),
  "text" varchar(50) COLLATE "pg_catalog"."default",
  "windspeed" numeric(5,2),
  "api_source" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "fetch_time" timestamp(6) NOT NULL,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."weather_forecast"."fxtime" IS '预报时间（对应和风天气API fxTime字段）';
COMMENT ON COLUMN "public"."weather_forecast"."temp_max" IS '最高温度（℃）';
COMMENT ON COLUMN "public"."weather_forecast"."temp_min" IS '最低温度（℃）';
COMMENT ON COLUMN "public"."weather_forecast"."humidity" IS '湿度（%）';
COMMENT ON COLUMN "public"."weather_forecast"."pop" IS '降水概率（%）';
COMMENT ON COLUMN "public"."weather_forecast"."precip" IS '预计降雨量（mm）';
COMMENT ON COLUMN "public"."weather_forecast"."text" IS '天气描述';
COMMENT ON COLUMN "public"."weather_forecast"."windspeed" IS '风速（km/h）';
COMMENT ON COLUMN "public"."weather_forecast"."api_source" IS 'API来源：qweather（和风天气）';
COMMENT ON TABLE "public"."weather_forecast" IS '天气预报表';

-- ----------------------------
-- Function structure for _postgis_deprecate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_deprecate"("oldname" text, "newname" text, "version" text);
CREATE OR REPLACE FUNCTION "public"."_postgis_deprecate"("oldname" text, "newname" text, "version" text)
  RETURNS "pg_catalog"."void" AS $BODY$
DECLARE
  curver_text text;
BEGIN
  --
  -- Raises a NOTICE if it was deprecated in this version,
  -- a WARNING if in a previous version (only up to minor version checked)
  --
	curver_text := '3.6.3';
	IF pg_catalog.split_part(curver_text,'.',1)::int > pg_catalog.split_part(version,'.',1)::int OR
	   ( pg_catalog.split_part(curver_text,'.',1) = pg_catalog.split_part(version,'.',1) AND
		 pg_catalog.split_part(curver_text,'.',2) != split_part(version,'.',2) )
	THEN
	  RAISE WARNING '% signature was deprecated in %. Please use %', oldname, version, newname;
	ELSE
	  RAISE DEBUG '% signature was deprecated in %. Please use %', oldname, version, newname;
	END IF;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for _postgis_index_extent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_index_extent"("tbl" regclass, "col" text);
CREATE OR REPLACE FUNCTION "public"."_postgis_index_extent"("tbl" regclass, "col" text)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', '_postgis_gserialized_index_extent'
  LANGUAGE c STABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for _postgis_join_selectivity
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_join_selectivity"(regclass, text, regclass, text, text);
CREATE OR REPLACE FUNCTION "public"."_postgis_join_selectivity"(regclass, text, regclass, text, text='2'::text)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', '_postgis_gserialized_joinsel'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for _postgis_pgsql_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_pgsql_version"();
CREATE OR REPLACE FUNCTION "public"."_postgis_pgsql_version"()
  RETURNS "pg_catalog"."text" AS $BODY$
	SELECT CASE WHEN pg_catalog.split_part(s,'.',1)::integer > 9 THEN pg_catalog.split_part(s,'.',1) || '0'
	ELSE pg_catalog.split_part(s,'.', 1) || pg_catalog.split_part(s,'.', 2) END AS v
	FROM pg_catalog.substring(version(), E'PostgreSQL ([0-9\\.]+)') AS s;
$BODY$
  LANGUAGE sql STABLE
  COST 100;

-- ----------------------------
-- Function structure for _postgis_scripts_pgsql_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_scripts_pgsql_version"();
CREATE OR REPLACE FUNCTION "public"."_postgis_scripts_pgsql_version"()
  RETURNS "pg_catalog"."text" AS $BODY$SELECT '160'::text AS version$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for _postgis_selectivity
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_selectivity"("tbl" regclass, "att_name" text, "geom" "public"."geometry", "mode" text);
CREATE OR REPLACE FUNCTION "public"."_postgis_selectivity"("tbl" regclass, "att_name" text, "geom" "public"."geometry", "mode" text='2'::text)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', '_postgis_gserialized_sel'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for _postgis_stats
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_postgis_stats"("tbl" regclass, "att_name" text, text);
CREATE OR REPLACE FUNCTION "public"."_postgis_stats"("tbl" regclass, "att_name" text, text='2'::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', '_postgis_gserialized_stats'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for _st_3ddfullywithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_3ddfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."_st_3ddfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dfullywithin3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_3ddwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_3ddwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."_st_3ddwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dwithin3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_3dintersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_3dintersects"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_3dintersects"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_3DIntersects'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_asgml"(int4, "public"."geometry", int4, int4, text, text);
CREATE OR REPLACE FUNCTION "public"."_st_asgml"(int4, "public"."geometry", int4, int4, text, text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asGML'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for _st_asx3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_asx3d"(int4, "public"."geometry", int4, int4, text);
CREATE OR REPLACE FUNCTION "public"."_st_asx3d"(int4, "public"."geometry", int4, int4, text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asX3D'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for _st_bestsrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_bestsrid"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_bestsrid"("public"."geography")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'geography_bestsrid'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for _st_bestsrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_bestsrid"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_bestsrid"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'geography_bestsrid'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for _st_contains
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_contains"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_contains"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'contains'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_containsproperly
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_containsproperly"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_containsproperly"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'containsproperly'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_coveredby
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_coveredby"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_coveredby"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_coveredby'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_coveredby
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_coveredby"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_coveredby"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'coveredby'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_covers
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_covers"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_covers"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_covers'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_covers
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_covers"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_covers"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'covers'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_crosses
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_crosses"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_crosses"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'crosses'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_dfullywithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_dfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."_st_dfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dfullywithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_distancetree
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_distancetree"("public"."geography", "public"."geography", float8, bool);
CREATE OR REPLACE FUNCTION "public"."_st_distancetree"("public"."geography", "public"."geography", float8, bool)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_distance_tree'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_distancetree
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_distancetree"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_distancetree"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."float8" AS $BODY$SELECT public._ST_DistanceTree($1, $2, 0.0, true)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for _st_distanceuncached
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_distanceuncached"("public"."geography", "public"."geography", bool);
CREATE OR REPLACE FUNCTION "public"."_st_distanceuncached"("public"."geography", "public"."geography", bool)
  RETURNS "pg_catalog"."float8" AS $BODY$SELECT public._ST_DistanceUnCached($1, $2, 0.0, $3)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for _st_distanceuncached
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_distanceuncached"("public"."geography", "public"."geography", float8, bool);
CREATE OR REPLACE FUNCTION "public"."_st_distanceuncached"("public"."geography", "public"."geography", float8, bool)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_distance_uncached'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_distanceuncached
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_distanceuncached"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_distanceuncached"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."float8" AS $BODY$SELECT public._ST_DistanceUnCached($1, $2, 0.0, true)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for _st_dwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_dwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."_st_dwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dwithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_dwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_dwithin"("geog1" "public"."geography", "geog2" "public"."geography", "tolerance" float8, "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."_st_dwithin"("geog1" "public"."geography", "geog2" "public"."geography", "tolerance" float8, "use_spheroid" bool=true)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_dwithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_dwithinuncached
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_dwithinuncached"("public"."geography", "public"."geography", float8);
CREATE OR REPLACE FUNCTION "public"."_st_dwithinuncached"("public"."geography", "public"."geography", float8)
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $1 OPERATOR(public.&&) public._ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public._ST_Expand($1,$3) AND public._ST_DWithinUnCached($1, $2, $3, true)$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for _st_dwithinuncached
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_dwithinuncached"("public"."geography", "public"."geography", float8, bool);
CREATE OR REPLACE FUNCTION "public"."_st_dwithinuncached"("public"."geography", "public"."geography", float8, bool)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_dwithin_uncached'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_equals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_equals"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_equals"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_Equals'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_expand"("public"."geography", float8);
CREATE OR REPLACE FUNCTION "public"."_st_expand"("public"."geography", float8)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for _st_geomfromgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_geomfromgml"(text, int4);
CREATE OR REPLACE FUNCTION "public"."_st_geomfromgml"(text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geom_from_gml'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for _st_intersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_intersects"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_intersects"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_Intersects'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_linecrossingdirection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_linecrossingdirection"("line1" "public"."geometry", "line2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_linecrossingdirection"("line1" "public"."geometry", "line2" "public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_LineCrossingDirection'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_longestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_longestline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_longestline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_longestline2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for _st_maxdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_maxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_maxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_maxdistance2d_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for _st_orderingequals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_orderingequals"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_orderingequals"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_same'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_overlaps
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_pointoutside
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_pointoutside"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."_st_pointoutside"("public"."geography")
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_point_outside'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for _st_sortablehash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_sortablehash"("geom" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_sortablehash"("geom" "public"."geometry")
  RETURNS "pg_catalog"."int8" AS '$libdir/postgis-3', '_ST_SortableHash'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for _st_touches
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_touches"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_touches"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'touches'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for _st_voronoi
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_voronoi"("g1" "public"."geometry", "clip" "public"."geometry", "tolerance" float8, "return_polygons" bool);
CREATE OR REPLACE FUNCTION "public"."_st_voronoi"("g1" "public"."geometry", "clip" "public"."geometry"=NULL::geometry, "tolerance" float8=0.0, "return_polygons" bool=true)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Voronoi'
  LANGUAGE c IMMUTABLE
  COST 5000;

-- ----------------------------
-- Function structure for _st_within
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."_st_within"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."_st_within"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT public._ST_Contains($2,$1)$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for addgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."addgeometrycolumn"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid_in" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool);
CREATE OR REPLACE FUNCTION "public"."addgeometrycolumn"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid_in" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool=true)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	rec RECORD;
	sr varchar;
	real_schema name;
	sql text;
	new_srid integer;

BEGIN

	-- Verify geometry type
	IF (public.postgis_type_name(new_type,new_dim) IS NULL )
	THEN
		RAISE EXCEPTION 'Invalid type name "%(%)" - valid ones are:
	POINT, MULTIPOINT,
	LINESTRING, MULTILINESTRING,
	POLYGON, MULTIPOLYGON,
	CIRCULARSTRING, COMPOUNDCURVE, MULTICURVE,
	CURVEPOLYGON, MULTISURFACE,
	GEOMETRY, GEOMETRYCOLLECTION,
	POINTM, MULTIPOINTM,
	LINESTRINGM, MULTILINESTRINGM,
	POLYGONM, MULTIPOLYGONM,
	CIRCULARSTRINGM, COMPOUNDCURVEM, MULTICURVEM
	CURVEPOLYGONM, MULTISURFACEM, TRIANGLE, TRIANGLEM,
	POLYHEDRALSURFACE, POLYHEDRALSURFACEM, TIN, TINM
	or GEOMETRYCOLLECTIONM', new_type, new_dim;
		RETURN 'fail';
	END IF;

	-- Verify dimension
	IF ( (new_dim >4) OR (new_dim <2) ) THEN
		RAISE EXCEPTION 'invalid dimension';
		RETURN 'fail';
	END IF;

	IF ( (new_type LIKE '%M') AND (new_dim!=3) ) THEN
		RAISE EXCEPTION 'TypeM needs 3 dimensions';
		RETURN 'fail';
	END IF;

	-- Verify SRID
	IF ( new_srid_in > 0 ) THEN
		IF new_srid_in > 998999 THEN
			RAISE EXCEPTION 'AddGeometryColumn() - SRID must be <= %', 998999;
		END IF;
		new_srid := new_srid_in;
		SELECT SRID INTO sr FROM public.spatial_ref_sys WHERE SRID = new_srid;
		IF NOT FOUND THEN
			RAISE EXCEPTION 'AddGeometryColumn() - invalid SRID';
			RETURN 'fail';
		END IF;
	ELSE
		new_srid := public.ST_SRID('POINT EMPTY'::public.geometry);
		IF ( new_srid_in != new_srid ) THEN
			RAISE NOTICE 'SRID value % converted to the officially unknown SRID value %', new_srid_in, new_srid;
		END IF;
	END IF;

	-- Verify schema
	IF ( schema_name IS NOT NULL AND schema_name != '' ) THEN
		sql := 'SELECT nspname FROM pg_namespace ' ||
			'WHERE text(nspname) = ' || quote_literal(schema_name) ||
			'LIMIT 1';
		RAISE DEBUG '%', sql;
		EXECUTE sql INTO real_schema;

		IF ( real_schema IS NULL ) THEN
			RAISE EXCEPTION 'Schema % is not a valid schemaname', quote_literal(schema_name);
			RETURN 'fail';
		END IF;
	END IF;

	IF ( real_schema IS NULL ) THEN
		RAISE DEBUG 'Detecting schema';
		sql := 'SELECT n.nspname AS schemaname ' ||
			'FROM pg_catalog.pg_class c ' ||
			  'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace ' ||
			'WHERE c.relkind = ' || quote_literal('r') ||
			' AND n.nspname NOT IN (' || quote_literal('pg_catalog') || ', ' || quote_literal('pg_toast') || ')' ||
			' AND pg_catalog.pg_table_is_visible(c.oid)' ||
			' AND c.relname = ' || quote_literal(table_name);
		RAISE DEBUG '%', sql;
		EXECUTE sql INTO real_schema;

		IF ( real_schema IS NULL ) THEN
			RAISE EXCEPTION 'Table % does not occur in the search_path', quote_literal(table_name);
			RETURN 'fail';
		END IF;
	END IF;

	-- Add geometry column to table
	IF use_typmod THEN
		 sql := 'ALTER TABLE ' ||
			quote_ident(real_schema) || '.' || quote_ident(table_name)
			|| ' ADD COLUMN ' || quote_ident(column_name) ||
			' geometry(' || public.postgis_type_name(new_type, new_dim) || ', ' || new_srid::text || ')';
		RAISE DEBUG '%', sql;
	ELSE
		sql := 'ALTER TABLE ' ||
			quote_ident(real_schema) || '.' || quote_ident(table_name)
			|| ' ADD COLUMN ' || quote_ident(column_name) ||
			' geometry ';
		RAISE DEBUG '%', sql;
	END IF;
	EXECUTE sql;

	IF NOT use_typmod THEN
		-- Add table CHECKs
		sql := 'ALTER TABLE ' ||
			quote_ident(real_schema) || '.' || quote_ident(table_name)
			|| ' ADD CONSTRAINT '
			|| quote_ident('enforce_srid_' || column_name)
			|| ' CHECK (st_srid(' || quote_ident(column_name) ||
			') = ' || new_srid::text || ')' ;
		RAISE DEBUG '%', sql;
		EXECUTE sql;

		sql := 'ALTER TABLE ' ||
			quote_ident(real_schema) || '.' || quote_ident(table_name)
			|| ' ADD CONSTRAINT '
			|| quote_ident('enforce_dims_' || column_name)
			|| ' CHECK (st_ndims(' || quote_ident(column_name) ||
			') = ' || new_dim::text || ')' ;
		RAISE DEBUG '%', sql;
		EXECUTE sql;

		IF ( NOT (new_type = 'GEOMETRY')) THEN
			sql := 'ALTER TABLE ' ||
				quote_ident(real_schema) || '.' || quote_ident(table_name) || ' ADD CONSTRAINT ' ||
				quote_ident('enforce_geotype_' || column_name) ||
				' CHECK (GeometryType(' ||
				quote_ident(column_name) || ')=' ||
				quote_literal(new_type) || ' OR (' ||
				quote_ident(column_name) || ') is null)';
			RAISE DEBUG '%', sql;
			EXECUTE sql;
		END IF;
	END IF;

	RETURN
		real_schema || '.' ||
		table_name || '.' || column_name ||
		' SRID:' || new_srid::text ||
		' TYPE:' || new_type ||
		' DIMS:' || new_dim::text || ' ';
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for addgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."addgeometrycolumn"("table_name" varchar, "column_name" varchar, "new_srid" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool);
CREATE OR REPLACE FUNCTION "public"."addgeometrycolumn"("table_name" varchar, "column_name" varchar, "new_srid" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool=true)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret  text;
BEGIN
	SELECT public.AddGeometryColumn('','',$1,$2,$3,$4,$5, $6) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for addgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."addgeometrycolumn"("schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool);
CREATE OR REPLACE FUNCTION "public"."addgeometrycolumn"("schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid" int4, "new_type" varchar, "new_dim" int4, "use_typmod" bool=true)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret  text;
BEGIN
	SELECT public.AddGeometryColumn('',$1,$2,$3,$4,$5,$6,$7) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql STABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_numeric, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_halfvec"(_numeric, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float8, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_halfvec"(_float8, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_halfvec"(_float4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_int4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_halfvec"(_int4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_numeric, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_sparsevec"(_numeric, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_int4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_sparsevec"(_int4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_sparsevec"(_float4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float8, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_sparsevec"(_float8, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_numeric, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_vector"(_numeric, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_int4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_vector"(_int4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float4, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_vector"(_float4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float8, int4, bool);
CREATE OR REPLACE FUNCTION "public"."array_to_vector"(_float8, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."binary_quantize"("public"."halfvec")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'halfvec_binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."binary_quantize"("public"."vector")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."box"("public"."geometry")
  RETURNS "pg_catalog"."box" AS '$libdir/postgis-3', 'LWGEOM_to_BOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."box"("public"."box3d")
  RETURNS "pg_catalog"."box" AS '$libdir/postgis-3', 'BOX3D_to_BOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2d"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."box2d"("public"."box3d")
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX3D_to_BOX2D'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."box2d"("public"."geometry")
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'LWGEOM_to_BOX2D'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box2d_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2d_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."box2d_in"(cstring)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX2D_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box2d_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2d_out"("public"."box2d");
CREATE OR REPLACE FUNCTION "public"."box2d_out"("public"."box2d")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'BOX2D_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box2df_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2df_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."box2df_in"(cstring)
  RETURNS "public"."box2df" AS '$libdir/postgis-3', 'box2df_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box2df_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box2df_out"("public"."box2df");
CREATE OR REPLACE FUNCTION "public"."box2df_out"("public"."box2df")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'box2df_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box3d"("public"."box2d");
CREATE OR REPLACE FUNCTION "public"."box3d"("public"."box2d")
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX2D_to_BOX3D'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box3d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."box3d"("public"."geometry")
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'LWGEOM_to_BOX3D'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for box3d_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box3d_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."box3d_in"(cstring)
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box3d_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box3d_out"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."box3d_out"("public"."box3d")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'BOX3D_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for box3dtobox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."box3dtobox"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."box3dtobox"("public"."box3d")
  RETURNS "pg_catalog"."box" AS '$libdir/postgis-3', 'BOX3D_to_BOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."bytea"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."bytea"("public"."geometry")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_to_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."bytea"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."bytea"("public"."geography")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_to_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for contains_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."contains_2d"("public"."box2df", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."contains_2d"("public"."box2df", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_box2df_geom_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for contains_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."contains_2d"("public"."box2df", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."contains_2d"("public"."box2df", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_box2df_box2df_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for contains_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."contains_2d"("public"."geometry", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."contains_2d"("public"."geometry", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $2 OPERATOR(public.@) $1;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."cosine_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."cosine_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for dropgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrycolumn"("schema_name" varchar, "table_name" varchar, "column_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrycolumn"("schema_name" varchar, "table_name" varchar, "column_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret text;
BEGIN
	SELECT public.DropGeometryColumn('',$1,$2,$3) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for dropgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrycolumn"("table_name" varchar, "column_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrycolumn"("table_name" varchar, "column_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret text;
BEGIN
	SELECT public.DropGeometryColumn('','',$1,$2) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for dropgeometrycolumn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrycolumn"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrycolumn"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	myrec RECORD;
	okay boolean;
	real_schema name;

BEGIN

	-- Find, check or fix schema_name
	IF ( schema_name != '' ) THEN
		okay = false;

		FOR myrec IN SELECT nspname FROM pg_namespace WHERE text(nspname) = schema_name LOOP
			okay := true;
		END LOOP;

		IF ( okay <>  true ) THEN
			RAISE NOTICE 'Invalid schema name - using current_schema()';
			SELECT current_schema() into real_schema;
		ELSE
			real_schema = schema_name;
		END IF;
	ELSE
		SELECT current_schema() into real_schema;
	END IF;

	-- Find out if the column is in the geometry_columns table
	okay = false;
	FOR myrec IN SELECT * from public.geometry_columns where f_table_schema = text(real_schema) and f_table_name = table_name and f_geometry_column = column_name LOOP
		okay := true;
	END LOOP;
	IF (okay <> true) THEN
		RAISE EXCEPTION 'column not found in geometry_columns table';
		RETURN false;
	END IF;

	-- Remove table column
	EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) || '.' ||
		quote_ident(table_name) || ' DROP COLUMN ' ||
		quote_ident(column_name);

	RETURN real_schema || '.' || table_name || '.' || column_name ||' effectively removed.';

END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for dropgeometrytable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrytable"("schema_name" varchar, "table_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrytable"("schema_name" varchar, "table_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.DropGeometryTable('',$1,$2) $BODY$
  LANGUAGE sql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for dropgeometrytable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrytable"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrytable"("catalog_name" varchar, "schema_name" varchar, "table_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	real_schema name;

BEGIN

	IF ( schema_name = '' ) THEN
		SELECT current_schema() into real_schema;
	ELSE
		real_schema = schema_name;
	END IF;

	-- TODO: Should we warn if table doesn't exist probably instead just saying dropped
	-- Remove table
	EXECUTE 'DROP TABLE IF EXISTS '
		|| quote_ident(real_schema) || '.' ||
		quote_ident(table_name) || ' RESTRICT';

	RETURN
		real_schema || '.' ||
		table_name ||' dropped.';

END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for dropgeometrytable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."dropgeometrytable"("table_name" varchar);
CREATE OR REPLACE FUNCTION "public"."dropgeometrytable"("table_name" varchar)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.DropGeometryTable('','',$1) $BODY$
  LANGUAGE sql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for equals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."equals"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."equals"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_Equals'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for find_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."find_srid"(varchar, varchar, varchar);
CREATE OR REPLACE FUNCTION "public"."find_srid"(varchar, varchar, varchar)
  RETURNS "pg_catalog"."int4" AS $BODY$
DECLARE
	schem varchar =  $1;
	tabl varchar = $2;
	sr int4;
BEGIN
-- if the table contains a . and the schema is empty
-- split the table into a schema and a table
-- otherwise drop through to default behavior
	IF ( schem = '' and strpos(tabl,'.') > 0 ) THEN
	 schem = substr(tabl,1,strpos(tabl,'.')-1);
	 tabl = substr(tabl,length(schem)+2);
	END IF;

	select SRID into sr from public.geometry_columns where (f_table_schema = schem or schem = '') and f_table_name = tabl and f_geometry_column = $3;
	IF NOT FOUND THEN
	   RAISE EXCEPTION 'find_srid() - could not find the corresponding SRID - is the geometry registered in the GEOMETRY_COLUMNS table?  Is there an uppercase/lowercase mismatch?';
	END IF;
	return sr;
END;
$BODY$
  LANGUAGE plpgsql STABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for geog_brin_inclusion_add_value
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geog_brin_inclusion_add_value"(internal, internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geog_brin_inclusion_add_value"(internal, internal, internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geog_brin_inclusion_add_value'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geog_brin_inclusion_merge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geog_brin_inclusion_merge"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geog_brin_inclusion_merge"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'geog_brin_inclusion_merge'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography"("public"."geography", int4, bool);
CREATE OR REPLACE FUNCTION "public"."geography"("public"."geography", int4, bool)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_enforce_typmod'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geography"("public"."geometry")
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_from_geometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography"(bytea);
CREATE OR REPLACE FUNCTION "public"."geography"(bytea)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_from_binary'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_analyze
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_analyze"(internal);
CREATE OR REPLACE FUNCTION "public"."geography_analyze"(internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_analyze_nd'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_cmp"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_cmp"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'geography_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_distance_knn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_distance_knn"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_distance_knn"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_distance_knn'
  LANGUAGE c IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for geography_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_eq"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_eq"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_ge"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_ge"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_compress
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_compress"(internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_compress"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_compress'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_consistent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_consistent"(internal, "public"."geography", int4);
CREATE OR REPLACE FUNCTION "public"."geography_gist_consistent"(internal, "public"."geography", int4)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gist_consistent'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_decompress
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_decompress"(internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_decompress"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_decompress'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_distance"(internal, "public"."geography", int4);
CREATE OR REPLACE FUNCTION "public"."geography_gist_distance"(internal, "public"."geography", int4)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_geog_distance'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_penalty
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_penalty"(internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_penalty"(internal, internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_penalty'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_picksplit
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_picksplit"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_picksplit"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_picksplit'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_same
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_same"("public"."box2d", "public"."box2d", internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_same"("public"."box2d", "public"."box2d", internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_same'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gist_union
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gist_union"(bytea, internal);
CREATE OR REPLACE FUNCTION "public"."geography_gist_union"(bytea, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_union'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geography_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_gt"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_gt"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_in"(cstring, oid, int4);
CREATE OR REPLACE FUNCTION "public"."geography_in"(cstring, oid, int4)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_le"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_le"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_lt"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_lt"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_out"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_out"("public"."geography")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'geography_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_overlaps
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_overlaps"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_overlaps"("public"."geography", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_recv"(internal, oid, int4);
CREATE OR REPLACE FUNCTION "public"."geography_recv"(internal, oid, int4)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_send"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."geography_send"("public"."geography")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'geography_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_choose_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_choose_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_choose_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_choose_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_compress_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_compress_nd"(internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_compress_nd"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_spgist_compress_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_config_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_config_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_config_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_config_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_inner_consistent_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_inner_consistent_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_inner_consistent_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_inner_consistent_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_leaf_consistent_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_leaf_consistent_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_leaf_consistent_nd"(internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_spgist_leaf_consistent_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_spgist_picksplit_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_spgist_picksplit_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geography_spgist_picksplit_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_picksplit_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_typmod_in"(_cstring);
CREATE OR REPLACE FUNCTION "public"."geography_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'geography_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geography_typmod_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geography_typmod_out"(int4);
CREATE OR REPLACE FUNCTION "public"."geography_typmod_out"(int4)
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'postgis_typmod_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geom2d_brin_inclusion_add_value
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom2d_brin_inclusion_add_value"(internal, internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom2d_brin_inclusion_add_value"(internal, internal, internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geom2d_brin_inclusion_add_value'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geom2d_brin_inclusion_merge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom2d_brin_inclusion_merge"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom2d_brin_inclusion_merge"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'geom2d_brin_inclusion_merge'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geom3d_brin_inclusion_add_value
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom3d_brin_inclusion_add_value"(internal, internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom3d_brin_inclusion_add_value"(internal, internal, internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geom3d_brin_inclusion_add_value'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geom3d_brin_inclusion_merge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom3d_brin_inclusion_merge"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom3d_brin_inclusion_merge"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'geom3d_brin_inclusion_merge'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geom4d_brin_inclusion_add_value
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom4d_brin_inclusion_add_value"(internal, internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom4d_brin_inclusion_add_value"(internal, internal, internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geom4d_brin_inclusion_add_value'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geom4d_brin_inclusion_merge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geom4d_brin_inclusion_merge"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geom4d_brin_inclusion_merge"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'geom4d_brin_inclusion_merge'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"(text);
CREATE OR REPLACE FUNCTION "public"."geometry"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'parse_WKT_lwgeom'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."geometry"("public"."geography")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geometry_from_geography'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"("public"."geometry", int4, bool);
CREATE OR REPLACE FUNCTION "public"."geometry"("public"."geometry", int4, bool)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geometry_enforce_typmod'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"(point);
CREATE OR REPLACE FUNCTION "public"."geometry"(point)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'point_to_geometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"(path);
CREATE OR REPLACE FUNCTION "public"."geometry"(path)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'path_to_geometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"(polygon);
CREATE OR REPLACE FUNCTION "public"."geometry"(polygon)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'polygon_to_geometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"(bytea);
CREATE OR REPLACE FUNCTION "public"."geometry"(bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."geometry"("public"."box3d")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'BOX3D_to_LWGEOM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry"("public"."box2d");
CREATE OR REPLACE FUNCTION "public"."geometry"("public"."box2d")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'BOX2D_to_LWGEOM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for geometry_above
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_above"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_above"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_above_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_analyze
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_analyze"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_analyze"(internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_analyze_nd'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_below
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_below"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_below"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_below_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_cmp"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_cmp"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'lwgeom_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_contained_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_contained_3d"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_contained_3d"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contained_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_contains
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_contains"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_contains"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_contains_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_contains_3d"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_contains_3d"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_contains_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_contains_nd"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_contains_nd"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_distance_box
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_distance_box"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_distance_box"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_distance_box_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_distance_centroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_distance_centroid"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_distance_centroid"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_Distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for geometry_distance_centroid_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_distance_centroid_nd"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_distance_centroid_nd"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_distance_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_distance_cpa
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_distance_cpa"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_distance_cpa"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_DistanceCPA'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for geometry_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_eq"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_eq"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_ge"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_ge"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_compress_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_compress_2d"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_compress_2d"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_compress_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_compress_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_compress_nd"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_compress_nd"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_compress'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_consistent_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_consistent_2d"(internal, "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_consistent_2d"(internal, "public"."geometry", int4)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gist_consistent_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_consistent_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_consistent_nd"(internal, "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_consistent_nd"(internal, "public"."geometry", int4)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gist_consistent'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_decompress_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_decompress_2d"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_decompress_2d"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_decompress_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_decompress_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_decompress_nd"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_decompress_nd"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_decompress'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_distance_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_distance_2d"(internal, "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_distance_2d"(internal, "public"."geometry", int4)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_distance_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_distance_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_distance_nd"(internal, "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_distance_nd"(internal, "public"."geometry", int4)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_distance'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_penalty_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_penalty_2d"(internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_penalty_2d"(internal, internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_penalty_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_penalty_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_penalty_nd"(internal, internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_penalty_nd"(internal, internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_penalty'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_picksplit_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_picksplit_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_picksplit_2d"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_picksplit_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_picksplit_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_picksplit_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_picksplit_nd"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_picksplit'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_same_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_same_2d"("geom1" "public"."geometry", "geom2" "public"."geometry", internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_same_2d"("geom1" "public"."geometry", "geom2" "public"."geometry", internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_same_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_same_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_same_nd"("public"."geometry", "public"."geometry", internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_same_nd"("public"."geometry", "public"."geometry", internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_same'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_sortsupport_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_sortsupport_2d"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_sortsupport_2d"(internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_gist_sortsupport_2d'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_union_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_union_2d"(bytea, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_union_2d"(bytea, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_union_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gist_union_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gist_union_nd"(bytea, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_gist_union_nd"(bytea, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_gist_union'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for geometry_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_gt"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_gt"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_hash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_hash"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_hash"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'lwgeom_hash'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."geometry_in"(cstring)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_le"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_le"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_left
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_left"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_left"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_left_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_lt"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_lt"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_neq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_neq"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_neq"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'lwgeom_neq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_out"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_out"("public"."geometry")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'LWGEOM_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overabove
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overabove"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overabove"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overabove_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overbelow
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overbelow"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overbelow"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overbelow_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overlaps
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overlaps_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overlaps_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overlaps_3d"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overlaps_3d"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overlaps_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overlaps_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overlaps_nd"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overlaps_nd"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overleft
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overleft"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overleft"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overleft_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_overright
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_overright"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_overright"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overright_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_recv"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_recv"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_right
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_right"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_right"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_right_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_same
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_same"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_same"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_same_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_same_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_same_3d"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_same_3d"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_same_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_same_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_same_nd"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_same_nd"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_same'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_send"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_send"("public"."geometry")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_sortsupport
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_sortsupport"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_sortsupport"(internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'lwgeom_sortsupport'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_choose_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_choose_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_choose_2d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_choose_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_choose_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_choose_3d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_choose_3d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_choose_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_choose_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_choose_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_choose_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_choose_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_compress_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_compress_2d"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_compress_2d"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_spgist_compress_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_compress_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_compress_3d"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_compress_3d"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_spgist_compress_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_compress_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_compress_nd"(internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_compress_nd"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'gserialized_spgist_compress_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_config_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_config_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_config_2d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_config_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_config_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_config_3d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_config_3d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_config_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_config_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_config_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_config_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_config_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_inner_consistent_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_inner_consistent_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_inner_consistent_2d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_inner_consistent_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_inner_consistent_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_inner_consistent_3d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_inner_consistent_3d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_inner_consistent_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_inner_consistent_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_inner_consistent_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_inner_consistent_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_inner_consistent_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_leaf_consistent_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_leaf_consistent_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_leaf_consistent_2d"(internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_spgist_leaf_consistent_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_leaf_consistent_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_leaf_consistent_3d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_leaf_consistent_3d"(internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_spgist_leaf_consistent_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_leaf_consistent_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_leaf_consistent_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_leaf_consistent_nd"(internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_spgist_leaf_consistent_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_picksplit_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_picksplit_2d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_picksplit_2d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_picksplit_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_picksplit_3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_picksplit_3d"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_picksplit_3d"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_picksplit_3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_spgist_picksplit_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_spgist_picksplit_nd"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."geometry_spgist_picksplit_nd"(internal, internal)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'gserialized_spgist_picksplit_nd'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_typmod_in"(_cstring);
CREATE OR REPLACE FUNCTION "public"."geometry_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'geometry_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_typmod_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_typmod_out"(int4);
CREATE OR REPLACE FUNCTION "public"."geometry_typmod_out"(int4)
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'postgis_typmod_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_within
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_within"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_within"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_within_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometry_within_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometry_within_nd"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometry_within_nd"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_within'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometrytype
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometrytype"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."geometrytype"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_getTYPE'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geometrytype
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geometrytype"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."geometrytype"("public"."geography")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_getTYPE'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for geomfromewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geomfromewkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."geomfromewkb"(bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOMFromEWKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for geomfromewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."geomfromewkt"(text);
CREATE OR REPLACE FUNCTION "public"."geomfromewkt"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'parse_WKT_lwgeom'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for get_proj4_from_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."get_proj4_from_srid"(int4);
CREATE OR REPLACE FUNCTION "public"."get_proj4_from_srid"(int4)
  RETURNS "pg_catalog"."text" AS $BODY$
	BEGIN
	RETURN proj4text::text FROM public.spatial_ref_sys WHERE srid= $1;
	END;
	$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for gidx_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gidx_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."gidx_in"(cstring)
  RETURNS "public"."gidx" AS '$libdir/postgis-3', 'gidx_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for gidx_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gidx_out"("public"."gidx");
CREATE OR REPLACE FUNCTION "public"."gidx_out"("public"."gidx")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'gidx_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for gserialized_gist_joinsel_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gserialized_gist_joinsel_2d"(internal, oid, internal, int2);
CREATE OR REPLACE FUNCTION "public"."gserialized_gist_joinsel_2d"(internal, oid, internal, int2)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_joinsel_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for gserialized_gist_joinsel_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gserialized_gist_joinsel_nd"(internal, oid, internal, int2);
CREATE OR REPLACE FUNCTION "public"."gserialized_gist_joinsel_nd"(internal, oid, internal, int2)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_joinsel_nd'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for gserialized_gist_sel_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gserialized_gist_sel_2d"(internal, oid, internal, int4);
CREATE OR REPLACE FUNCTION "public"."gserialized_gist_sel_2d"(internal, oid, internal, int4)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_sel_2d'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for gserialized_gist_sel_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gserialized_gist_sel_nd"(internal, oid, internal, int4);
CREATE OR REPLACE FUNCTION "public"."gserialized_gist_sel_nd"(internal, oid, internal, int4)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'gserialized_gist_sel_nd'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec"("public"."halfvec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."halfvec"("public"."halfvec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_accum"(_float8, "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_accum"(_float8, "public"."halfvec")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'halfvec_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_add"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_add"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_avg"(_float8);
CREATE OR REPLACE FUNCTION "public"."halfvec_avg"(_float8)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_combine"(_float8, _float8);
CREATE OR REPLACE FUNCTION "public"."halfvec_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_concat"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_concat"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_eq"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_eq"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ge"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_ge"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_gt"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_gt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_in"(cstring, oid, int4);
CREATE OR REPLACE FUNCTION "public"."halfvec_in"(cstring, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_le"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_le"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_lt"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_lt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_mul"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_mul"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ne"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_ne"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_out"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_out"("public"."halfvec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'halfvec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_recv"(internal, oid, int4);
CREATE OR REPLACE FUNCTION "public"."halfvec_recv"(internal, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_send"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_send"("public"."halfvec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'halfvec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_sub"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."halfvec_sub"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_float4"("public"."halfvec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."halfvec_to_float4"("public"."halfvec", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'halfvec_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'halfvec_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_vector"("public"."halfvec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."halfvec_to_vector"("public"."halfvec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'halfvec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_typmod_in"(_cstring);
CREATE OR REPLACE FUNCTION "public"."halfvec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hamming_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hamming_distance"(bit, bit);
CREATE OR REPLACE FUNCTION "public"."hamming_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'hamming_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hnsw_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_bit_support"(internal);
CREATE OR REPLACE FUNCTION "public"."hnsw_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_bit_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnsw_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_halfvec_support"(internal);
CREATE OR REPLACE FUNCTION "public"."hnsw_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnsw_sparsevec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_sparsevec_support"(internal);
CREATE OR REPLACE FUNCTION "public"."hnsw_sparsevec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_sparsevec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnswhandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnswhandler"(internal);
CREATE OR REPLACE FUNCTION "public"."hnswhandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'hnswhandler'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for is_contained_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."is_contained_2d"("public"."geometry", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."is_contained_2d"("public"."geometry", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $2 OPERATOR(public.~) $1;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for is_contained_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."is_contained_2d"("public"."box2df", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."is_contained_2d"("public"."box2df", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_within_box2df_geom_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for is_contained_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."is_contained_2d"("public"."box2df", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."is_contained_2d"("public"."box2df", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_box2df_box2df_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for ivfflat_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_bit_support"(internal);
CREATE OR REPLACE FUNCTION "public"."ivfflat_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_bit_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for ivfflat_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_halfvec_support"(internal);
CREATE OR REPLACE FUNCTION "public"."ivfflat_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for ivfflathandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflathandler"(internal);
CREATE OR REPLACE FUNCTION "public"."ivfflathandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'ivfflathandler'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for jaccard_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."jaccard_distance"(bit, bit);
CREATE OR REPLACE FUNCTION "public"."jaccard_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'jaccard_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for json
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."json"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."json"("public"."geometry")
  RETURNS "pg_catalog"."json" AS '$libdir/postgis-3', 'geometry_to_json'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for jsonb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."jsonb"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."jsonb"("public"."geometry")
  RETURNS "pg_catalog"."jsonb" AS '$libdir/postgis-3', 'geometry_to_jsonb'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."l1_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."l1_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."l1_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."l2_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."l2_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."halfvec", "public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."l2_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."l2_norm"("public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."l2_norm"("public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."l2_normalize"("public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."l2_normalize"("public"."sparsevec")
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."l2_normalize"("public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_2d"("public"."box2df", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."overlaps_2d"("public"."box2df", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_overlaps_box2df_geom_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_2d"("public"."geometry", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."overlaps_2d"("public"."geometry", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $2 OPERATOR(public.&&) $1;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_2d"("public"."box2df", "public"."box2df");
CREATE OR REPLACE FUNCTION "public"."overlaps_2d"("public"."box2df", "public"."box2df")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_contains_box2df_box2df_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_geog
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_geog"("public"."gidx", "public"."gidx");
CREATE OR REPLACE FUNCTION "public"."overlaps_geog"("public"."gidx", "public"."gidx")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gidx_gidx_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_geog
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_geog"("public"."geography", "public"."gidx");
CREATE OR REPLACE FUNCTION "public"."overlaps_geog"("public"."geography", "public"."gidx")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $2 OPERATOR(public.&&) $1;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for overlaps_geog
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_geog"("public"."gidx", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."overlaps_geog"("public"."gidx", "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gidx_geog_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_nd"("public"."geometry", "public"."gidx");
CREATE OR REPLACE FUNCTION "public"."overlaps_nd"("public"."geometry", "public"."gidx")
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT $2 OPERATOR(public.&&&) $1;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_nd"("public"."gidx", "public"."gidx");
CREATE OR REPLACE FUNCTION "public"."overlaps_nd"("public"."gidx", "public"."gidx")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gidx_gidx_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for overlaps_nd
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."overlaps_nd"("public"."gidx", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."overlaps_nd"("public"."gidx", "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'gserialized_gidx_geom_overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for path
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."path"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."path"("public"."geometry")
  RETURNS "pg_catalog"."path" AS '$libdir/postgis-3', 'geometry_to_path'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgis_asflatgeobuf_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asflatgeobuf_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asflatgeobuf_finalfn"(internal)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'pgis_asflatgeobuf_finalfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asflatgeobuf_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asflatgeobuf_transfn"(internal, anyelement);
CREATE OR REPLACE FUNCTION "public"."pgis_asflatgeobuf_transfn"(internal, anyelement)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asflatgeobuf_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_asflatgeobuf_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asflatgeobuf_transfn"(internal, anyelement, bool, text);
CREATE OR REPLACE FUNCTION "public"."pgis_asflatgeobuf_transfn"(internal, anyelement, bool, text)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asflatgeobuf_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_asflatgeobuf_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asflatgeobuf_transfn"(internal, anyelement, bool);
CREATE OR REPLACE FUNCTION "public"."pgis_asflatgeobuf_transfn"(internal, anyelement, bool)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asflatgeobuf_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_asgeobuf_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asgeobuf_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asgeobuf_finalfn"(internal)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'pgis_asgeobuf_finalfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asgeobuf_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asgeobuf_transfn"(internal, anyelement);
CREATE OR REPLACE FUNCTION "public"."pgis_asgeobuf_transfn"(internal, anyelement)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asgeobuf_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_asgeobuf_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asgeobuf_transfn"(internal, anyelement, text);
CREATE OR REPLACE FUNCTION "public"."pgis_asgeobuf_transfn"(internal, anyelement, text)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asgeobuf_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_asmvt_combinefn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_combinefn"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_combinefn"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_combinefn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_deserialfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_deserialfn"(bytea, internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_deserialfn"(bytea, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_deserialfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_finalfn"(internal)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'pgis_asmvt_finalfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_serialfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_serialfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_serialfn"(internal)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'pgis_asmvt_serialfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_transfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4, text, text);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4, text, text)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_transfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_transfn"(internal, anyelement);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_transfn"(internal, anyelement)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_transfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4, text);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_transfn"(internal, anyelement, text, int4, text)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_transfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_asmvt_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_asmvt_transfn"(internal, anyelement, text);
CREATE OR REPLACE FUNCTION "public"."pgis_asmvt_transfn"(internal, anyelement, text)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_asmvt_transfn'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_accum_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry", float8, int4);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry", float8, int4)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_accum_transfn'
  LANGUAGE c VOLATILE
  COST 50;

-- ----------------------------
-- Function structure for pgis_geometry_accum_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry", float8)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_accum_transfn'
  LANGUAGE c VOLATILE
  COST 50;

-- ----------------------------
-- Function structure for pgis_geometry_accum_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_accum_transfn"(internal, "public"."geometry")
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_accum_transfn'
  LANGUAGE c VOLATILE
  COST 50;

-- ----------------------------
-- Function structure for pgis_geometry_clusterintersecting_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_clusterintersecting_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_clusterintersecting_finalfn"(internal)
  RETURNS "public"."_geometry" AS '$libdir/postgis-3', 'pgis_geometry_clusterintersecting_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_clusterwithin_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_clusterwithin_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_clusterwithin_finalfn"(internal)
  RETURNS "public"."_geometry" AS '$libdir/postgis-3', 'pgis_geometry_clusterwithin_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_collect_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_collect_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_collect_finalfn"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_geometry_collect_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_coverageunion_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_coverageunion_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_coverageunion_finalfn"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_geometry_coverageunion_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_makeline_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_makeline_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_makeline_finalfn"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_geometry_makeline_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_polygonize_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_polygonize_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_polygonize_finalfn"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_geometry_polygonize_finalfn'
  LANGUAGE c VOLATILE
  COST 250;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_combinefn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_combinefn"(internal, internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_combinefn"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_combinefn'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_deserialfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_deserialfn"(bytea, internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_deserialfn"(bytea, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_deserialfn'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_finalfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_finalfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_finalfn"(internal)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_finalfn'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_serialfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_serialfn"(internal);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_serialfn"(internal)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_serialfn'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_transfn"(internal, "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_transfn"(internal, "public"."geometry", float8)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_transfn'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for pgis_geometry_union_parallel_transfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."pgis_geometry_union_parallel_transfn"(internal, "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."pgis_geometry_union_parallel_transfn"(internal, "public"."geometry")
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'pgis_geometry_union_parallel_transfn'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for point
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."point"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."point"("public"."geometry")
  RETURNS "pg_catalog"."point" AS '$libdir/postgis-3', 'geometry_to_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for polygon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."polygon"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."polygon"("public"."geometry")
  RETURNS "pg_catalog"."polygon" AS '$libdir/postgis-3', 'geometry_to_polygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for populate_geometry_columns
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."populate_geometry_columns"("use_typmod" bool);
CREATE OR REPLACE FUNCTION "public"."populate_geometry_columns"("use_typmod" bool=true)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	inserted	integer;
	oldcount	integer;
	probed	  integer;
	stale	   integer;
	gcs		 RECORD;
	gc		  RECORD;
	gsrid	   integer;
	gndims	  integer;
	gtype	   text;
	query	   text;
	gc_is_valid boolean;

BEGIN
	SELECT count(*) INTO oldcount FROM public.geometry_columns;
	inserted := 0;

	-- Count the number of geometry columns in all tables and views
	SELECT count(DISTINCT c.oid) INTO probed
	FROM pg_class c,
		 pg_attribute a,
		 pg_type t,
		 pg_namespace n
	WHERE c.relkind IN('r','v','f', 'p')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%' AND c.relname != 'raster_columns' ;

	-- Iterate through all non-dropped geometry columns
	RAISE DEBUG 'Processing Tables.....';

	FOR gcs IN
	SELECT DISTINCT ON (c.oid) c.oid, n.nspname, c.relname
		FROM pg_class c,
			 pg_attribute a,
			 pg_type t,
			 pg_namespace n
		WHERE c.relkind IN( 'r', 'f', 'p')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%' AND c.relname != 'raster_columns'
	LOOP

		inserted := inserted + public.populate_geometry_columns(gcs.oid, use_typmod);
	END LOOP;

	IF oldcount > inserted THEN
		stale = oldcount-inserted;
	ELSE
		stale = 0;
	END IF;

	RETURN 'probed:' ||probed|| ' inserted:'||inserted;
END

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for populate_geometry_columns
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."populate_geometry_columns"("tbl_oid" oid, "use_typmod" bool);
CREATE OR REPLACE FUNCTION "public"."populate_geometry_columns"("tbl_oid" oid, "use_typmod" bool=true)
  RETURNS "pg_catalog"."int4" AS $BODY$
DECLARE
	gcs		 RECORD;
	gc		  RECORD;
	gc_old	  RECORD;
	gsrid	   integer;
	gndims	  integer;
	gtype	   text;
	query	   text;
	gc_is_valid boolean;
	inserted	integer;
	constraint_successful boolean := false;

BEGIN
	inserted := 0;

	-- Iterate through all geometry columns in this table
	FOR gcs IN
	SELECT n.nspname, c.relname, a.attname, c.relkind
		FROM pg_class c,
			 pg_attribute a,
			 pg_type t,
			 pg_namespace n
		WHERE c.relkind IN('r', 'f', 'p')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%'
		AND c.oid = tbl_oid
	LOOP

		RAISE DEBUG 'Processing column %.%.%', gcs.nspname, gcs.relname, gcs.attname;

		gc_is_valid := true;
		-- Find the srid, coord_dimension, and type of current geometry
		-- in geometry_columns -- which is now a view

		SELECT type, srid, coord_dimension, gcs.relkind INTO gc_old
			FROM geometry_columns
			WHERE f_table_schema = gcs.nspname AND f_table_name = gcs.relname AND f_geometry_column = gcs.attname;

		IF upper(gc_old.type) = 'GEOMETRY' THEN
		-- This is an unconstrained geometry we need to do something
		-- We need to figure out what to set the type by inspecting the data
			EXECUTE 'SELECT public.ST_srid(' || quote_ident(gcs.attname) || ') As srid, public.GeometryType(' || quote_ident(gcs.attname) || ') As type, public.ST_NDims(' || quote_ident(gcs.attname) || ') As dims ' ||
					 ' FROM ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) ||
					 ' WHERE ' || quote_ident(gcs.attname) || ' IS NOT NULL LIMIT 1;'
				INTO gc;
			IF gc IS NULL THEN -- there is no data so we can not determine geometry type
				RAISE WARNING 'No data in table %.%, so no information to determine geometry type and srid', gcs.nspname, gcs.relname;
				RETURN 0;
			END IF;
			gsrid := gc.srid; gtype := gc.type; gndims := gc.dims;

			IF use_typmod THEN
				BEGIN
					EXECUTE 'ALTER TABLE ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || ' ALTER COLUMN ' || quote_ident(gcs.attname) ||
						' TYPE geometry(' || public.postgis_type_name(gtype, gndims, true) || ', ' || gsrid::text  || ') ';
					inserted := inserted + 1;
				EXCEPTION
						WHEN invalid_parameter_value OR feature_not_supported THEN
						RAISE WARNING 'Could not convert ''%'' in ''%.%'' to use typmod with srid %, type %: %', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), gsrid, public.postgis_type_name(gtype, gndims, true), SQLERRM;
							gc_is_valid := false;
				END;

			ELSE
				-- Try to apply srid check to column
				constraint_successful = false;
				IF (gsrid > 0 AND public.postgis_constraint_srid(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
					BEGIN
						EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) ||
								 ' ADD CONSTRAINT ' || quote_ident('enforce_srid_' || gcs.attname) ||
								 ' CHECK (ST_srid(' || quote_ident(gcs.attname) || ') = ' || gsrid || ')';
						constraint_successful := true;
					EXCEPTION
						WHEN check_violation THEN
							RAISE WARNING 'Not inserting ''%'' in ''%.%'' into geometry_columns: could not apply constraint CHECK (st_srid(%) = %)', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), quote_ident(gcs.attname), gsrid;
							gc_is_valid := false;
					END;
				END IF;

				-- Try to apply ndims check to column
				IF (gndims IS NOT NULL AND public.postgis_constraint_dims(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
					BEGIN
						EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || '
								 ADD CONSTRAINT ' || quote_ident('enforce_dims_' || gcs.attname) || '
								 CHECK (st_ndims(' || quote_ident(gcs.attname) || ') = '||gndims||')';
						constraint_successful := true;
					EXCEPTION
						WHEN check_violation THEN
							RAISE WARNING 'Not inserting ''%'' in ''%.%'' into geometry_columns: could not apply constraint CHECK (st_ndims(%) = %)', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), quote_ident(gcs.attname), gndims;
							gc_is_valid := false;
					END;
				END IF;

				-- Try to apply geometrytype check to column
				IF (gtype IS NOT NULL AND public.postgis_constraint_type(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
					BEGIN
						EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || '
						ADD CONSTRAINT ' || quote_ident('enforce_geotype_' || gcs.attname) || '
						CHECK (geometrytype(' || quote_ident(gcs.attname) || ') = ' || quote_literal(gtype) || ')';
						constraint_successful := true;
					EXCEPTION
						WHEN check_violation THEN
							-- No geometry check can be applied. This column contains a number of geometry types.
							RAISE WARNING 'Could not add geometry type check (%) to table column: %.%.%', gtype, quote_ident(gcs.nspname),quote_ident(gcs.relname),quote_ident(gcs.attname);
					END;
				END IF;
				 --only count if we were successful in applying at least one constraint
				IF constraint_successful THEN
					inserted := inserted + 1;
				END IF;
			END IF;
		END IF;

	END LOOP;

	RETURN inserted;
END

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for postgis_addbbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_addbbox"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_addbbox"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_addBBOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for postgis_cache_bbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_cache_bbox"();
CREATE OR REPLACE FUNCTION "public"."postgis_cache_bbox"()
  RETURNS "pg_catalog"."trigger" AS '$libdir/postgis-3', 'cache_bbox'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for postgis_constraint_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_constraint_dims"("geomschema" text, "geomtable" text, "geomcolumn" text);
CREATE OR REPLACE FUNCTION "public"."postgis_constraint_dims"("geomschema" text, "geomtable" text, "geomcolumn" text)
  RETURNS "pg_catalog"."int4" AS $BODY$
SELECT  replace(split_part(s.consrc, ' = ', 2), ')', '')::integer
		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%ndims(% = %';
$BODY$
  LANGUAGE sql STABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for postgis_constraint_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_constraint_srid"("geomschema" text, "geomtable" text, "geomcolumn" text);
CREATE OR REPLACE FUNCTION "public"."postgis_constraint_srid"("geomschema" text, "geomtable" text, "geomcolumn" text)
  RETURNS "pg_catalog"."int4" AS $BODY$
SELECT replace(replace(split_part(s.consrc, ' = ', 2), ')', ''), '(', '')::integer
		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%srid(% = %';
$BODY$
  LANGUAGE sql STABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for postgis_constraint_type
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_constraint_type"("geomschema" text, "geomtable" text, "geomcolumn" text);
CREATE OR REPLACE FUNCTION "public"."postgis_constraint_type"("geomschema" text, "geomtable" text, "geomcolumn" text)
  RETURNS "pg_catalog"."varchar" AS $BODY$
SELECT  replace(split_part(s.consrc, '''', 2), ')', '')::varchar
		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%geometrytype(% = %';
$BODY$
  LANGUAGE sql STABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for postgis_dropbbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_dropbbox"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_dropbbox"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_dropBBOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for postgis_extensions_upgrade
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_extensions_upgrade"("target_version" text);
CREATE OR REPLACE FUNCTION "public"."postgis_extensions_upgrade"("target_version" text=NULL::text)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	rec record;
	sql text;
	var_schema text;
BEGIN

	FOR rec IN
		SELECT name, default_version, installed_version
		FROM pg_catalog.pg_available_extensions
		WHERE name IN (
			'postgis',
			'postgis_raster',
			'postgis_sfcgal',
			'postgis_topology',
			'postgis_tiger_geocoder'
		)
		ORDER BY length(name) -- this is to make sure 'postgis' is first !
	LOOP --{

		IF target_version IS NULL THEN
			target_version := rec.default_version;
		END IF;

		IF rec.installed_version IS NULL THEN --{
			-- If the support installed by available extension
			-- is found unpackaged, we package it
			IF --{
				 -- PostGIS is always available (this function is part of it)
				 rec.name = 'postgis'

				 -- PostGIS raster is available if type 'raster' exists
				 OR ( rec.name = 'postgis_raster' AND EXISTS (
							SELECT 1 FROM pg_catalog.pg_type
							WHERE typname = 'raster' ) )

				 -- PostGIS SFCGAL is available if
				 -- 'postgis_sfcgal_version' function exists
				 OR ( rec.name = 'postgis_sfcgal' AND EXISTS (
							SELECT 1 FROM pg_catalog.pg_proc
							WHERE proname = 'postgis_sfcgal_version' ) )

				 -- PostGIS Topology is available if
				 -- 'topology.topology' table exists
				 -- NOTE: watch out for https://trac.osgeo.org/postgis/ticket/2503
				 OR ( rec.name = 'postgis_topology' AND EXISTS (
							SELECT 1 FROM pg_catalog.pg_class c
							JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid )
							WHERE n.nspname = 'topology' AND c.relname = 'topology') )

				 OR ( rec.name = 'postgis_tiger_geocoder' AND EXISTS (
							SELECT 1 FROM pg_catalog.pg_class c
							JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid )
							WHERE n.nspname = 'tiger' AND c.relname = 'geocode_settings') )
			THEN --}{ -- the code is unpackaged
				-- Force install in same schema as postgis
				SELECT INTO var_schema n.nspname
				  FROM pg_namespace n, pg_proc p
				  WHERE p.proname = 'postgis_full_version'
					AND n.oid = p.pronamespace
				  LIMIT 1;
				IF rec.name NOT IN('postgis_topology', 'postgis_tiger_geocoder')
				THEN
					sql := format(
							  'CREATE EXTENSION %1$I SCHEMA %2$I VERSION unpackaged;'
							  'ALTER EXTENSION %1$I UPDATE TO %3$I',
							  rec.name, var_schema, target_version);
				ELSE
					sql := format(
							 'CREATE EXTENSION %1$I VERSION unpackaged;'
							 'ALTER EXTENSION %1$I UPDATE TO %2$I',
							 rec.name, target_version);
				END IF;
				RAISE NOTICE 'Packaging and updating %', rec.name;
				RAISE DEBUG '%', sql;
				EXECUTE sql;
			ELSE
				RAISE DEBUG 'Skipping % (not in use)', rec.name;
			END IF; --}
		ELSE -- The code is already packaged, upgrade it --}{
			sql = format(
				'ALTER EXTENSION %1$I UPDATE TO "ANY";'
				'ALTER EXTENSION %1$I UPDATE TO %2$I',
				rec.name, target_version
				);
			RAISE NOTICE 'Updating extension % %', rec.name, rec.installed_version;
			RAISE DEBUG '%', sql;
			EXECUTE sql;
		END IF; --}

	END LOOP; --}

	RETURN format(
		'Upgrade to version %s completed, run SELECT postgis_full_version(); for details',
		target_version
	);


END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for postgis_full_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_full_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_full_version"()
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	libver text;
	librev text;
	projver text;
	projver_compiled text;
	geosver text;
	geosver_compiled text;
	sfcgalver text;
	gdalver text := NULL;
	libxmlver text;
	liblwgeomver text;
	dbproc text;
	relproc text;
	fullver text;
	rast_lib_ver text := NULL;
	rast_scr_ver text := NULL;
	topo_scr_ver text := NULL;
	json_lib_ver text;
	protobuf_lib_ver text;
	wagyu_lib_ver text;
	sfcgal_lib_ver text;
	sfcgal_scr_ver text;
	pgsql_scr_ver text;
	pgsql_ver text;
	core_is_extension bool;
BEGIN
	SELECT public.postgis_lib_version() INTO libver;
	SELECT public.postgis_proj_version() INTO projver;
	SELECT public.postgis_geos_version() INTO geosver;
	SELECT public.postgis_geos_compiled_version() INTO geosver_compiled;
	SELECT public.postgis_proj_compiled_version() INTO projver_compiled;
	SELECT public.postgis_libjson_version() INTO json_lib_ver;
	SELECT public.postgis_libprotobuf_version() INTO protobuf_lib_ver;
	SELECT public.postgis_wagyu_version() INTO wagyu_lib_ver;
	SELECT public._postgis_scripts_pgsql_version() INTO pgsql_scr_ver;
	SELECT public._postgis_pgsql_version() INTO pgsql_ver;
	BEGIN
		SELECT public.postgis_gdal_version() INTO gdalver;
	EXCEPTION
		WHEN undefined_function THEN
			RAISE DEBUG 'Function postgis_gdal_version() not found.  Is raster support enabled and rtpostgis.sql installed?';
	END;
	BEGIN
		SELECT public.postgis_sfcgal_full_version() INTO sfcgalver;
		BEGIN
			SELECT public.postgis_sfcgal_scripts_installed() INTO sfcgal_scr_ver;
		EXCEPTION
			WHEN undefined_function THEN
				sfcgal_scr_ver := 'missing';
		END;
	EXCEPTION
		WHEN undefined_function THEN
			RAISE DEBUG 'Function postgis_sfcgal_scripts_installed() not found. Is sfcgal support enabled and sfcgal.sql installed?';
	END;
	SELECT public.postgis_liblwgeom_version() INTO liblwgeomver;
	SELECT public.postgis_libxml_version() INTO libxmlver;
	SELECT public.postgis_scripts_installed() INTO dbproc;
	SELECT public.postgis_scripts_released() INTO relproc;
	SELECT public.postgis_lib_revision() INTO librev;
	BEGIN
		SELECT topology.postgis_topology_scripts_installed() INTO topo_scr_ver;
	EXCEPTION
		WHEN undefined_function OR invalid_schema_name THEN
			RAISE DEBUG 'Function postgis_topology_scripts_installed() not found. Is topology support enabled and topology.sql installed?';
		WHEN insufficient_privilege THEN
			RAISE NOTICE 'Topology support cannot be inspected. Is current user granted USAGE on schema "topology" ?';
		WHEN OTHERS THEN
			RAISE NOTICE 'Function postgis_topology_scripts_installed() could not be called: % (%)', SQLERRM, SQLSTATE;
	END;

	BEGIN
		SELECT public.postgis_raster_scripts_installed() INTO rast_scr_ver;
	EXCEPTION
		WHEN undefined_function THEN
			RAISE DEBUG 'Function postgis_raster_scripts_installed() not found. Is raster support enabled and rtpostgis.sql installed?';
		WHEN OTHERS THEN
			RAISE NOTICE 'Function postgis_raster_scripts_installed() could not be called: % (%)', SQLERRM, SQLSTATE;
	END;

	BEGIN
		SELECT public.postgis_raster_lib_version() INTO rast_lib_ver;
	EXCEPTION
		WHEN undefined_function THEN
			RAISE DEBUG 'Function postgis_raster_lib_version() not found. Is raster support enabled and rtpostgis.sql installed?';
		WHEN OTHERS THEN
			RAISE NOTICE 'Function postgis_raster_lib_version() could not be called: % (%)', SQLERRM, SQLSTATE;
	END;

	fullver = 'POSTGIS="' || libver;

	IF  librev IS NOT NULL THEN
		fullver = fullver || ' ' || librev;
	END IF;

	fullver = fullver || '"';

	IF EXISTS (
		SELECT * FROM pg_catalog.pg_extension
		WHERE extname = 'postgis')
	THEN
			fullver = fullver || ' [EXTENSION]';
			core_is_extension := true;
	ELSE
			core_is_extension := false;
	END IF;

	IF liblwgeomver != relproc THEN
		fullver = fullver || ' (liblwgeom version mismatch: "' || liblwgeomver || '")';
	END IF;

	fullver = fullver || ' PGSQL="' || pgsql_scr_ver || '"';
	IF pgsql_scr_ver != pgsql_ver THEN
		fullver = fullver || ' (procs need upgrade for use with PostgreSQL "' || pgsql_ver || '")';
	END IF;

	IF  geosver IS NOT NULL THEN
		fullver = fullver || ' GEOS="' || geosver || '"';
		IF (pg_catalog.string_to_array(geosver, '.'))[1:2] != (pg_catalog.string_to_array(geosver_compiled, '.'))[1:2]
		THEN
			fullver = pg_catalog.format('%s (compiled against GEOS %s)', fullver, geosver_compiled);
		END IF;
	END IF;

	IF  sfcgalver IS NOT NULL THEN
		fullver = fullver || ' SFCGAL="' || sfcgalver || '"';
	END IF;

	IF  projver IS NOT NULL THEN
		fullver = fullver || ' PROJ="' || projver || '"';
		IF (pg_catalog.string_to_array(projver, '.'))[1:3] != (pg_catalog.string_to_array(projver_compiled, '.'))[1:3]
		THEN
			fullver = pg_catalog.format('%s (compiled against PROJ %s)', fullver, projver_compiled);
		END IF;
	END IF;

	IF  gdalver IS NOT NULL THEN
		fullver = fullver || ' GDAL="' || gdalver || '"';
	END IF;

	IF  libxmlver IS NOT NULL THEN
		fullver = fullver || ' LIBXML="' || libxmlver || '"';
	END IF;

	IF json_lib_ver IS NOT NULL THEN
		fullver = fullver || ' LIBJSON="' || json_lib_ver || '"';
	END IF;

	IF protobuf_lib_ver IS NOT NULL THEN
		fullver = fullver || ' LIBPROTOBUF="' || protobuf_lib_ver || '"';
	END IF;

	IF wagyu_lib_ver IS NOT NULL THEN
		fullver = fullver || ' WAGYU="' || wagyu_lib_ver || '"';
	END IF;

	IF dbproc != relproc THEN
		fullver = fullver || ' (core procs from "' || dbproc || '" need upgrade)';
	END IF;

	IF topo_scr_ver IS NOT NULL THEN
		fullver = fullver || ' TOPOLOGY';
		IF topo_scr_ver != relproc THEN
			fullver = fullver || ' (topology procs from "' || topo_scr_ver || '" need upgrade)';
		END IF;
		IF core_is_extension AND NOT EXISTS (
			SELECT * FROM pg_catalog.pg_extension
			WHERE extname = 'postgis_topology')
		THEN
				fullver = fullver || ' [UNPACKAGED!]';
		END IF;
	END IF;

	IF rast_lib_ver IS NOT NULL THEN
		fullver = fullver || ' RASTER';
		IF rast_lib_ver != relproc THEN
			fullver = fullver || ' (raster lib from "' || rast_lib_ver || '" need upgrade)';
		END IF;
		IF core_is_extension AND NOT EXISTS (
			SELECT * FROM pg_catalog.pg_extension
			WHERE extname = 'postgis_raster')
		THEN
				fullver = fullver || ' [UNPACKAGED!]';
		END IF;
	END IF;

	IF rast_scr_ver IS NOT NULL AND rast_scr_ver != relproc THEN
		fullver = fullver || ' (raster procs from "' || rast_scr_ver || '" need upgrade)';
	END IF;

	IF sfcgal_scr_ver IS NOT NULL AND sfcgal_scr_ver != relproc THEN
		fullver = fullver || ' (sfcgal procs from "' || sfcgal_scr_ver || '" need upgrade)';
	END IF;

	-- Check for the presence of deprecated functions
	IF EXISTS ( SELECT oid FROM pg_catalog.pg_proc WHERE proname LIKE '%_deprecated_by_postgis_%' )
	THEN
		fullver = fullver || ' (deprecated functions exist, upgrade is not complete)';
	END IF;

	RETURN fullver;
END
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for postgis_geos_compiled_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_geos_compiled_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_geos_compiled_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_geos_compiled_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_geos_noop
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_geos_noop"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_geos_noop"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'GEOSnoop'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_geos_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_geos_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_geos_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_geos_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_getbbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_getbbox"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_getbbox"("public"."geometry")
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'LWGEOM_to_BOX2DF'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_hasbbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_hasbbox"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_hasbbox"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_hasBBOX'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_index_supportfn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_index_supportfn"(internal);
CREATE OR REPLACE FUNCTION "public"."postgis_index_supportfn"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/postgis-3', 'postgis_index_supportfn'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for postgis_lib_build_date
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_lib_build_date"();
CREATE OR REPLACE FUNCTION "public"."postgis_lib_build_date"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_lib_build_date'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_lib_revision
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_lib_revision"();
CREATE OR REPLACE FUNCTION "public"."postgis_lib_revision"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_lib_revision'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_lib_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_lib_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_lib_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_lib_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_libjson_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_libjson_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_libjson_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_libjson_version'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_liblwgeom_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_liblwgeom_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_liblwgeom_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_liblwgeom_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_libprotobuf_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_libprotobuf_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_libprotobuf_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_libprotobuf_version'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_libxml_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_libxml_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_libxml_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_libxml_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_noop
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_noop"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."postgis_noop"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_noop'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_proj_compiled_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_proj_compiled_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_proj_compiled_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_proj_compiled_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_proj_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_proj_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_proj_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_proj_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_scripts_build_date
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_scripts_build_date"();
CREATE OR REPLACE FUNCTION "public"."postgis_scripts_build_date"()
  RETURNS "pg_catalog"."text" AS $BODY$SELECT '2026-04-15 03:38:50'::text AS version$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for postgis_scripts_installed
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_scripts_installed"();
CREATE OR REPLACE FUNCTION "public"."postgis_scripts_installed"()
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT trim('3.6.3'::text || $rev$ 3d12666 $rev$) AS version $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for postgis_scripts_released
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_scripts_released"();
CREATE OR REPLACE FUNCTION "public"."postgis_scripts_released"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_scripts_released'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_srs
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_srs"("auth_name" text, "auth_srid" text);
CREATE OR REPLACE FUNCTION "public"."postgis_srs"("auth_name" text, "auth_srid" text)
  RETURNS TABLE("auth_name" text, "auth_srid" text, "srname" text, "srtext" text, "proj4text" text, "point_sw" "public"."geometry", "point_ne" "public"."geometry") AS '$libdir/postgis-3', 'postgis_srs_entry'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for postgis_srs_all
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_srs_all"();
CREATE OR REPLACE FUNCTION "public"."postgis_srs_all"()
  RETURNS TABLE("auth_name" text, "auth_srid" text, "srname" text, "srtext" text, "proj4text" text, "point_sw" "public"."geometry", "point_ne" "public"."geometry") AS '$libdir/postgis-3', 'postgis_srs_entry_all'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000
  ROWS 1000;

-- ----------------------------
-- Function structure for postgis_srs_codes
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_srs_codes"("auth_name" text);
CREATE OR REPLACE FUNCTION "public"."postgis_srs_codes"("auth_name" text)
  RETURNS SETOF "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_srs_codes'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for postgis_srs_search
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_srs_search"("bounds" "public"."geometry", "authname" text);
CREATE OR REPLACE FUNCTION "public"."postgis_srs_search"("bounds" "public"."geometry", "authname" text='EPSG'::text)
  RETURNS TABLE("auth_name" text, "auth_srid" text, "srname" text, "srtext" text, "proj4text" text, "point_sw" "public"."geometry", "point_ne" "public"."geometry") AS '$libdir/postgis-3', 'postgis_srs_search'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000
  ROWS 1000;

-- ----------------------------
-- Function structure for postgis_svn_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_svn_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_svn_version"()
  RETURNS "pg_catalog"."text" AS $BODY$
	SELECT public._postgis_deprecate(
		'postgis_svn_version', 'postgis_lib_revision', '3.1.0');
	SELECT public.postgis_lib_revision();
$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for postgis_transform_geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_transform_geometry"("geom" "public"."geometry", text, text, int4);
CREATE OR REPLACE FUNCTION "public"."postgis_transform_geometry"("geom" "public"."geometry", text, text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'transform_geom'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for postgis_transform_pipeline_geometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_transform_pipeline_geometry"("geom" "public"."geometry", "pipeline" text, "forward" bool, "to_srid" int4);
CREATE OR REPLACE FUNCTION "public"."postgis_transform_pipeline_geometry"("geom" "public"."geometry", "pipeline" text, "forward" bool, "to_srid" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'transform_pipeline_geom'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for postgis_type_name
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_type_name"("geomname" varchar, "coord_dimension" int4, "use_new_name" bool);
CREATE OR REPLACE FUNCTION "public"."postgis_type_name"("geomname" varchar, "coord_dimension" int4, "use_new_name" bool=true)
  RETURNS "pg_catalog"."varchar" AS $BODY$
	SELECT CASE WHEN $3 THEN new_name ELSE old_name END As geomname
	FROM
	( VALUES
			('GEOMETRY', 'Geometry', 2),
			('GEOMETRY', 'GeometryZ', 3),
			('GEOMETRYM', 'GeometryM', 3),
			('GEOMETRY', 'GeometryZM', 4),

			('GEOMETRYCOLLECTION', 'GeometryCollection', 2),
			('GEOMETRYCOLLECTION', 'GeometryCollectionZ', 3),
			('GEOMETRYCOLLECTIONM', 'GeometryCollectionM', 3),
			('GEOMETRYCOLLECTION', 'GeometryCollectionZM', 4),

			('POINT', 'Point', 2),
			('POINT', 'PointZ', 3),
			('POINTM','PointM', 3),
			('POINT', 'PointZM', 4),

			('MULTIPOINT','MultiPoint', 2),
			('MULTIPOINT','MultiPointZ', 3),
			('MULTIPOINTM','MultiPointM', 3),
			('MULTIPOINT','MultiPointZM', 4),

			('POLYGON', 'Polygon', 2),
			('POLYGON', 'PolygonZ', 3),
			('POLYGONM', 'PolygonM', 3),
			('POLYGON', 'PolygonZM', 4),

			('MULTIPOLYGON', 'MultiPolygon', 2),
			('MULTIPOLYGON', 'MultiPolygonZ', 3),
			('MULTIPOLYGONM', 'MultiPolygonM', 3),
			('MULTIPOLYGON', 'MultiPolygonZM', 4),

			('MULTILINESTRING', 'MultiLineString', 2),
			('MULTILINESTRING', 'MultiLineStringZ', 3),
			('MULTILINESTRINGM', 'MultiLineStringM', 3),
			('MULTILINESTRING', 'MultiLineStringZM', 4),

			('LINESTRING', 'LineString', 2),
			('LINESTRING', 'LineStringZ', 3),
			('LINESTRINGM', 'LineStringM', 3),
			('LINESTRING', 'LineStringZM', 4),

			('CIRCULARSTRING', 'CircularString', 2),
			('CIRCULARSTRING', 'CircularStringZ', 3),
			('CIRCULARSTRINGM', 'CircularStringM' ,3),
			('CIRCULARSTRING', 'CircularStringZM', 4),

			('COMPOUNDCURVE', 'CompoundCurve', 2),
			('COMPOUNDCURVE', 'CompoundCurveZ', 3),
			('COMPOUNDCURVEM', 'CompoundCurveM', 3),
			('COMPOUNDCURVE', 'CompoundCurveZM', 4),

			('CURVEPOLYGON', 'CurvePolygon', 2),
			('CURVEPOLYGON', 'CurvePolygonZ', 3),
			('CURVEPOLYGONM', 'CurvePolygonM', 3),
			('CURVEPOLYGON', 'CurvePolygonZM', 4),

			('MULTICURVE', 'MultiCurve', 2),
			('MULTICURVE', 'MultiCurveZ', 3),
			('MULTICURVEM', 'MultiCurveM', 3),
			('MULTICURVE', 'MultiCurveZM', 4),

			('MULTISURFACE', 'MultiSurface', 2),
			('MULTISURFACE', 'MultiSurfaceZ', 3),
			('MULTISURFACEM', 'MultiSurfaceM', 3),
			('MULTISURFACE', 'MultiSurfaceZM', 4),

			('POLYHEDRALSURFACE', 'PolyhedralSurface', 2),
			('POLYHEDRALSURFACE', 'PolyhedralSurfaceZ', 3),
			('POLYHEDRALSURFACEM', 'PolyhedralSurfaceM', 3),
			('POLYHEDRALSURFACE', 'PolyhedralSurfaceZM', 4),

			('TRIANGLE', 'Triangle', 2),
			('TRIANGLE', 'TriangleZ', 3),
			('TRIANGLEM', 'TriangleM', 3),
			('TRIANGLE', 'TriangleZM', 4),

			('TIN', 'Tin', 2),
			('TIN', 'TinZ', 3),
			('TINM', 'TinM', 3),
			('TIN', 'TinZM', 4) )
			 As g(old_name, new_name, coord_dimension)
	WHERE (upper(old_name) = upper($1) OR upper(new_name) = upper($1))
		AND coord_dimension = $2;
$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for postgis_typmod_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_typmod_dims"(int4);
CREATE OR REPLACE FUNCTION "public"."postgis_typmod_dims"(int4)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'postgis_typmod_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_typmod_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_typmod_srid"(int4);
CREATE OR REPLACE FUNCTION "public"."postgis_typmod_srid"(int4)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'postgis_typmod_srid'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_typmod_type
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_typmod_type"(int4);
CREATE OR REPLACE FUNCTION "public"."postgis_typmod_type"(int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_typmod_type'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for postgis_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for postgis_wagyu_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."postgis_wagyu_version"();
CREATE OR REPLACE FUNCTION "public"."postgis_wagyu_version"()
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'postgis_wagyu_version'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec"("public"."sparsevec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."sparsevec"("public"."sparsevec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_in"(cstring, oid, int4);
CREATE OR REPLACE FUNCTION "public"."sparsevec_in"(cstring, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_out"("public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_out"("public"."sparsevec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'sparsevec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_recv"(internal, oid, int4);
CREATE OR REPLACE FUNCTION "public"."sparsevec_recv"(internal, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_send"("public"."sparsevec");
CREATE OR REPLACE FUNCTION "public"."sparsevec_send"("public"."sparsevec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'sparsevec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'sparsevec_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool);
CREATE OR REPLACE FUNCTION "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'sparsevec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_typmod_in"(_cstring);
CREATE OR REPLACE FUNCTION "public"."sparsevec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for spheroid_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."spheroid_in"(cstring);
CREATE OR REPLACE FUNCTION "public"."spheroid_in"(cstring)
  RETURNS "public"."spheroid" AS '$libdir/postgis-3', 'ellipsoid_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for spheroid_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."spheroid_out"("public"."spheroid");
CREATE OR REPLACE FUNCTION "public"."spheroid_out"("public"."spheroid")
  RETURNS "pg_catalog"."cstring" AS '$libdir/postgis-3', 'ellipsoid_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_3dclosestpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dclosestpoint"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dclosestpoint"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_closestpoint3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_3ddfullywithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3ddfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_3ddfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dfullywithin3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_3ddistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3ddistance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3ddistance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_3DDistance'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_3ddwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3ddwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_3ddwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dwithin3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_3dintersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dintersects"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dintersects"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_3DIntersects'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_3dlength
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dlength"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dlength"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_length_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_3dlineinterpolatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dlineinterpolatepoint"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_3dlineinterpolatepoint"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_3DLineInterpolatePoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_3dlongestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dlongestline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dlongestline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_longestline3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_3dmakebox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dmakebox"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dmakebox"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_construct'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_3dmaxdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dmaxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dmaxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_maxdistance3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_3dperimeter
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dperimeter"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dperimeter"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_perimeter_poly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_3dshortestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_3dshortestline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_3dshortestline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_shortestline3d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_addmeasure
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_addmeasure"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_addmeasure"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_AddMeasure'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_addpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_addpoint"("geom1" "public"."geometry", "geom2" "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_addpoint"("geom1" "public"."geometry", "geom2" "public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_addpoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_addpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_addpoint"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_addpoint"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_addpoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_affine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_affine"("public"."geometry", float8, float8, float8, float8, float8, float8, float8, float8, float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_affine"("public"."geometry", float8, float8, float8, float8, float8, float8, float8, float8, float8, float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_affine'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_affine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_affine"("public"."geometry", float8, float8, float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_affine"("public"."geometry", float8, float8, float8, float8, float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  $2, $3, 0,  $4, $5, 0,  0, 0, 1,  $6, $7, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_angle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_angle"("pt1" "public"."geometry", "pt2" "public"."geometry", "pt3" "public"."geometry", "pt4" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_angle"("pt1" "public"."geometry", "pt2" "public"."geometry", "pt3" "public"."geometry", "pt4" "public"."geometry"='0101000000000000000000F87F000000000000F87F'::geometry)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_angle'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_angle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_angle"("line1" "public"."geometry", "line2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_angle"("line1" "public"."geometry", "line2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS $BODY$SELECT public.ST_Angle(public.St_StartPoint($1), public.ST_EndPoint($1), public.ST_StartPoint($2), public.ST_EndPoint($2))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_area
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_area"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_area"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_Area'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_area
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_area"(text);
CREATE OR REPLACE FUNCTION "public"."st_area"(text)
  RETURNS "pg_catalog"."float8" AS $BODY$ SELECT public.ST_Area($1::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_area
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_area"("geog" "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_area"("geog" "public"."geography", "use_spheroid" bool=true)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_area'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_area2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_area2d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_area2d"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_Area'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asbinary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asbinary"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_asbinary"("public"."geometry")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_asBinary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asbinary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asbinary"("public"."geography", text);
CREATE OR REPLACE FUNCTION "public"."st_asbinary"("public"."geography", text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_asBinary'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_asbinary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asbinary"("public"."geometry", text);
CREATE OR REPLACE FUNCTION "public"."st_asbinary"("public"."geometry", text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_asBinary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asbinary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asbinary"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_asbinary"("public"."geography")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'LWGEOM_asBinary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asencodedpolyline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asencodedpolyline"("geom" "public"."geometry", "nprecision" int4);
CREATE OR REPLACE FUNCTION "public"."st_asencodedpolyline"("geom" "public"."geometry", "nprecision" int4=5)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asEncodedPolyline'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkb"("public"."geometry", text);
CREATE OR REPLACE FUNCTION "public"."st_asewkb"("public"."geometry", text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'WKBFromLWGEOM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkb"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_asewkb"("public"."geometry")
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'WKBFromLWGEOM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkt"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_asewkt"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asEWKT'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkt"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_asewkt"("public"."geography")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asEWKT'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkt"("public"."geography", int4);
CREATE OR REPLACE FUNCTION "public"."st_asewkt"("public"."geography", int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asEWKT'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkt"(text);
CREATE OR REPLACE FUNCTION "public"."st_asewkt"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.ST_AsEWKT($1::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asewkt"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_asewkt"("public"."geometry", int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asEWKT'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgeojson"(text);
CREATE OR REPLACE FUNCTION "public"."st_asgeojson"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.ST_AsGeoJson($1::public.geometry, 9, 0);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgeojson"("geog" "public"."geography", "maxdecimaldigits" int4, "options" int4);
CREATE OR REPLACE FUNCTION "public"."st_asgeojson"("geog" "public"."geography", "maxdecimaldigits" int4=9, "options" int4=0)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geography_as_geojson'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgeojson"("geom" "public"."geometry", "maxdecimaldigits" int4, "options" int4);
CREATE OR REPLACE FUNCTION "public"."st_asgeojson"("geom" "public"."geometry", "maxdecimaldigits" int4=9, "options" int4=8)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asGeoJson'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgeojson"("r" record, "geom_column" text, "maxdecimaldigits" int4, "pretty_bool" bool, "id_column" text);
CREATE OR REPLACE FUNCTION "public"."st_asgeojson"("r" record, "geom_column" text=''::text, "maxdecimaldigits" int4=9, "pretty_bool" bool=false, "id_column" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'ST_AsGeoJsonRow'
  LANGUAGE c STABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgml"("geog" "public"."geography", "maxdecimaldigits" int4, "options" int4, "nprefix" text, "id" text);
CREATE OR REPLACE FUNCTION "public"."st_asgml"("geog" "public"."geography", "maxdecimaldigits" int4=15, "options" int4=0, "nprefix" text='gml'::text, "id" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geography_as_gml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgml"("geom" "public"."geometry", "maxdecimaldigits" int4, "options" int4);
CREATE OR REPLACE FUNCTION "public"."st_asgml"("geom" "public"."geometry", "maxdecimaldigits" int4=15, "options" int4=0)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asGML'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgml"("version" int4, "geom" "public"."geometry", "maxdecimaldigits" int4, "options" int4, "nprefix" text, "id" text);
CREATE OR REPLACE FUNCTION "public"."st_asgml"("version" int4, "geom" "public"."geometry", "maxdecimaldigits" int4=15, "options" int4=0, "nprefix" text=NULL::text, "id" text=NULL::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asGML'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgml"("version" int4, "geog" "public"."geography", "maxdecimaldigits" int4, "options" int4, "nprefix" text, "id" text);
CREATE OR REPLACE FUNCTION "public"."st_asgml"("version" int4, "geog" "public"."geography", "maxdecimaldigits" int4=15, "options" int4=0, "nprefix" text='gml'::text, "id" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geography_as_gml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asgml"(text);
CREATE OR REPLACE FUNCTION "public"."st_asgml"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public._ST_AsGML(2,$1::public.geometry,15,0, NULL, NULL);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_ashexewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ashexewkb"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_ashexewkb"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asHEXEWKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_ashexewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ashexewkb"("public"."geometry", text);
CREATE OR REPLACE FUNCTION "public"."st_ashexewkb"("public"."geometry", text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asHEXEWKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_askml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_askml"("geog" "public"."geography", "maxdecimaldigits" int4, "nprefix" text);
CREATE OR REPLACE FUNCTION "public"."st_askml"("geog" "public"."geography", "maxdecimaldigits" int4=15, "nprefix" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geography_as_kml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_askml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_askml"(text);
CREATE OR REPLACE FUNCTION "public"."st_askml"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.ST_AsKML($1::public.geometry, 15);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_askml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_askml"("geom" "public"."geometry", "maxdecimaldigits" int4, "nprefix" text);
CREATE OR REPLACE FUNCTION "public"."st_askml"("geom" "public"."geometry", "maxdecimaldigits" int4=15, "nprefix" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asKML'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_aslatlontext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_aslatlontext"("geom" "public"."geometry", "tmpl" text);
CREATE OR REPLACE FUNCTION "public"."st_aslatlontext"("geom" "public"."geometry", "tmpl" text=''::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_to_latlon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_asmarc21
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asmarc21"("geom" "public"."geometry", "format" text);
CREATE OR REPLACE FUNCTION "public"."st_asmarc21"("geom" "public"."geometry", "format" text='hdddmmss'::text)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'ST_AsMARC21'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_asmvtgeom
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asmvtgeom"("geom" "public"."geometry", "bounds" "public"."box2d", "extent" int4, "buffer" int4, "clip_geom" bool);
CREATE OR REPLACE FUNCTION "public"."st_asmvtgeom"("geom" "public"."geometry", "bounds" "public"."box2d", "extent" int4=4096, "buffer" int4=256, "clip_geom" bool=true)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_AsMVTGeom'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_assvg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_assvg"(text);
CREATE OR REPLACE FUNCTION "public"."st_assvg"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.ST_AsSVG($1::public.geometry,0,15);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_assvg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_assvg"("geom" "public"."geometry", "rel" int4, "maxdecimaldigits" int4);
CREATE OR REPLACE FUNCTION "public"."st_assvg"("geom" "public"."geometry", "rel" int4=0, "maxdecimaldigits" int4=15)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asSVG'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_assvg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_assvg"("geog" "public"."geography", "rel" int4, "maxdecimaldigits" int4);
CREATE OR REPLACE FUNCTION "public"."st_assvg"("geog" "public"."geography", "rel" int4=0, "maxdecimaldigits" int4=15)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geography_as_svg'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astext"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_astext"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asText'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astext"(text);
CREATE OR REPLACE FUNCTION "public"."st_astext"(text)
  RETURNS "pg_catalog"."text" AS $BODY$ SELECT public.ST_AsText($1::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astext"("public"."geography", int4);
CREATE OR REPLACE FUNCTION "public"."st_astext"("public"."geography", int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asText'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astext"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_astext"("public"."geography")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asText'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astext"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_astext"("public"."geometry", int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_asText'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_astwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astwkb"("geom" "public"."geometry", "prec" int4, "prec_z" int4, "prec_m" int4, "with_sizes" bool, "with_boxes" bool);
CREATE OR REPLACE FUNCTION "public"."st_astwkb"("geom" "public"."geometry", "prec" int4=NULL::integer, "prec_z" int4=NULL::integer, "prec_m" int4=NULL::integer, "with_sizes" bool=NULL::boolean, "with_boxes" bool=NULL::boolean)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'TWKBFromLWGEOM'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_astwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_astwkb"("geom" "public"."_geometry", "ids" _int8, "prec" int4, "prec_z" int4, "prec_m" int4, "with_sizes" bool, "with_boxes" bool);
CREATE OR REPLACE FUNCTION "public"."st_astwkb"("geom" "public"."_geometry", "ids" _int8, "prec" int4=NULL::integer, "prec_z" int4=NULL::integer, "prec_m" int4=NULL::integer, "with_sizes" bool=NULL::boolean, "with_boxes" bool=NULL::boolean)
  RETURNS "pg_catalog"."bytea" AS '$libdir/postgis-3', 'TWKBFromLWGEOMArray'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_asx3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_asx3d"("geom" "public"."geometry", "maxdecimaldigits" int4, "options" int4);
CREATE OR REPLACE FUNCTION "public"."st_asx3d"("geom" "public"."geometry", "maxdecimaldigits" int4=15, "options" int4=0)
  RETURNS "pg_catalog"."text" AS $BODY$SELECT public._ST_AsX3D(3,$1,$2,$3,'');$BODY$
  LANGUAGE sql IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_azimuth
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_azimuth"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_azimuth"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_azimuth'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_azimuth
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_azimuth"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_azimuth"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_azimuth'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_bdmpolyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_bdmpolyfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_bdmpolyfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline public.geometry;
	geom public.geometry;
BEGIN
	mline := public.ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := public.ST_Multi(public.ST_BuildArea(mline));

	RETURN geom;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_bdpolyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_bdpolyfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_bdpolyfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline public.geometry;
	geom public.geometry;
BEGIN
	mline := public.ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := public.ST_BuildArea(mline);

	IF public.ST_GeometryType(geom) != 'ST_Polygon'
	THEN
		RAISE EXCEPTION 'Input returns more then a single polygon, try using BdMPolyFromText instead';
	END IF;

	RETURN geom;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_boundary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_boundary"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_boundary"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'boundary'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_boundingdiagonal
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_boundingdiagonal"("geom" "public"."geometry", "fits" bool);
CREATE OR REPLACE FUNCTION "public"."st_boundingdiagonal"("geom" "public"."geometry", "fits" bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_BoundingDiagonal'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_box2dfromgeohash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_box2dfromgeohash"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_box2dfromgeohash"(text, int4=NULL::integer)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'box2d_from_geohash'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"("public"."geography", float8, text);
CREATE OR REPLACE FUNCTION "public"."st_buffer"("public"."geography", float8, text)
  RETURNS "public"."geography" AS $BODY$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2, $3), public.ST_SRID($1)))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"("geom" "public"."geometry", "radius" float8, "quadsegs" int4);
CREATE OR REPLACE FUNCTION "public"."st_buffer"("geom" "public"."geometry", "radius" float8, "quadsegs" int4)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Buffer($1, $2, CAST('quad_segs='||CAST($3 AS text) as text)) $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"("geom" "public"."geometry", "radius" float8, "options" text);
CREATE OR REPLACE FUNCTION "public"."st_buffer"("geom" "public"."geometry", "radius" float8, "options" text=''::text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'buffer'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"(text, float8, text);
CREATE OR REPLACE FUNCTION "public"."st_buffer"(text, float8, text)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Buffer($1::public.geometry, $2, $3);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"(text, float8, int4);
CREATE OR REPLACE FUNCTION "public"."st_buffer"(text, float8, int4)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Buffer($1::public.geometry, $2, $3);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"(text, float8);
CREATE OR REPLACE FUNCTION "public"."st_buffer"(text, float8)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Buffer($1::public.geometry, $2);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"("public"."geography", float8);
CREATE OR REPLACE FUNCTION "public"."st_buffer"("public"."geography", float8)
  RETURNS "public"."geography" AS $BODY$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2), public.ST_SRID($1)))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buffer
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buffer"("public"."geography", float8, int4);
CREATE OR REPLACE FUNCTION "public"."st_buffer"("public"."geography", float8, int4)
  RETURNS "public"."geography" AS $BODY$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2, $3), public.ST_SRID($1)))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_buildarea
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_buildarea"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_buildarea"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_BuildArea'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_centroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_centroid"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_centroid"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'centroid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_centroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_centroid"("public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_centroid"("public"."geography", "use_spheroid" bool=true)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_centroid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_centroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_centroid"(text);
CREATE OR REPLACE FUNCTION "public"."st_centroid"(text)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Centroid($1::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_chaikinsmoothing
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_chaikinsmoothing"("public"."geometry", int4, bool);
CREATE OR REPLACE FUNCTION "public"."st_chaikinsmoothing"("public"."geometry", int4=1, bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_ChaikinSmoothing'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_cleangeometry
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_cleangeometry"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_cleangeometry"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CleanGeometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clipbybox2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clipbybox2d"("geom" "public"."geometry", "box" "public"."box2d");
CREATE OR REPLACE FUNCTION "public"."st_clipbybox2d"("geom" "public"."geometry", "box" "public"."box2d")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_ClipByBox2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_closestpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_closestpoint"("public"."geography", "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_closestpoint"("public"."geography", "public"."geography", "use_spheroid" bool=true)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_closestpoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_closestpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_closestpoint"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_closestpoint"(text, text)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_ClosestPoint($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_closestpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_closestpoint"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_closestpoint"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_closestpoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_closestpointofapproach
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_closestpointofapproach"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_closestpointofapproach"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_ClosestPointOfApproach'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterdbscan
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterdbscan"("public"."geometry", "eps" float8, "minpoints" int4);
CREATE OR REPLACE FUNCTION "public"."st_clusterdbscan"("public"."geometry", "eps" float8, "minpoints" int4)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_ClusterDBSCAN'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterintersecting
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterintersecting"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_clusterintersecting"("public"."_geometry")
  RETURNS "public"."_geometry" AS '$libdir/postgis-3', 'clusterintersecting_garray'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterintersectingwin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterintersectingwin"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_clusterintersectingwin"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_ClusterIntersectingWin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterkmeans
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterkmeans"("geom" "public"."geometry", "k" int4, "max_radius" float8);
CREATE OR REPLACE FUNCTION "public"."st_clusterkmeans"("geom" "public"."geometry", "k" int4, "max_radius" float8=NULL::double precision)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_ClusterKMeans'
  LANGUAGE c VOLATILE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterwithin"("public"."_geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_clusterwithin"("public"."_geometry", float8)
  RETURNS "public"."_geometry" AS '$libdir/postgis-3', 'cluster_within_distance_garray'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_clusterwithinwin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_clusterwithinwin"("public"."geometry", "distance" float8);
CREATE OR REPLACE FUNCTION "public"."st_clusterwithinwin"("public"."geometry", "distance" float8)
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_ClusterWithinWin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_collect
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_collect"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_collect"("public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_collect_garray'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_collect
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_collect"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_collect"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_collect'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_collectionextract
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_collectionextract"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_collectionextract"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CollectionExtract'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_collectionextract
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_collectionextract"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_collectionextract"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CollectionExtract'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_collectionhomogenize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_collectionhomogenize"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_collectionhomogenize"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CollectionHomogenize'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_combinebbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_combinebbox"("public"."box3d", "public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_combinebbox"("public"."box3d", "public"."box3d")
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_combine_BOX3D'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_combinebbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_combinebbox"("public"."box3d", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_combinebbox"("public"."box3d", "public"."geometry")
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_combine'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_combinebbox
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_combinebbox"("public"."box2d", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_combinebbox"("public"."box2d", "public"."geometry")
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX2D_combine'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for st_concavehull
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_concavehull"("param_geom" "public"."geometry", "param_pctconvex" float8, "param_allow_holes" bool);
CREATE OR REPLACE FUNCTION "public"."st_concavehull"("param_geom" "public"."geometry", "param_pctconvex" float8, "param_allow_holes" bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_ConcaveHull'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_contains
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_contains"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_contains"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'contains'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_containsproperly
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_containsproperly"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_containsproperly"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'containsproperly'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_convexhull
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_convexhull"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_convexhull"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'convexhull'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_coorddim
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coorddim"("geometry" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_coorddim"("geometry" "public"."geometry")
  RETURNS "pg_catalog"."int2" AS '$libdir/postgis-3', 'LWGEOM_ndims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_coverageclean
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coverageclean"("geom" "public"."geometry", "gapmaximumwidth" float8, "snappingdistance" float8, "overlapmergestrategy" text);
CREATE OR REPLACE FUNCTION "public"."st_coverageclean"("geom" "public"."geometry", "gapmaximumwidth" float8=0.0, "snappingdistance" float8='-1.0'::numeric, "overlapmergestrategy" text='MERGE_LONGEST_BORDER'::text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CoverageClean'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_coverageinvalidedges
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coverageinvalidedges"("geom" "public"."geometry", "tolerance" float8);
CREATE OR REPLACE FUNCTION "public"."st_coverageinvalidedges"("geom" "public"."geometry", "tolerance" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CoverageInvalidEdges'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_coveragesimplify
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coveragesimplify"("geom" "public"."geometry", "tolerance" float8, "simplifyboundary" bool);
CREATE OR REPLACE FUNCTION "public"."st_coveragesimplify"("geom" "public"."geometry", "tolerance" float8, "simplifyboundary" bool=true)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CoverageSimplify'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_coverageunion
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coverageunion"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_coverageunion"("public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CoverageUnion'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_coveredby
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coveredby"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_coveredby"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_coveredby'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_coveredby
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coveredby"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_coveredby"(text, text)
  RETURNS "pg_catalog"."bool" AS $BODY$ SELECT public.ST_CoveredBy($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_coveredby
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_coveredby"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_coveredby"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'coveredby'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_covers
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_covers"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_covers"(text, text)
  RETURNS "pg_catalog"."bool" AS $BODY$ SELECT public.ST_Covers($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_covers
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_covers"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_covers"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_covers'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_covers
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_covers"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_covers"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'covers'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_cpawithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_cpawithin"("public"."geometry", "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_cpawithin"("public"."geometry", "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_CPAWithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_crosses
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_crosses"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_crosses"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'crosses'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_curven
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_curven"("geometry" "public"."geometry", "i" int4);
CREATE OR REPLACE FUNCTION "public"."st_curven"("geometry" "public"."geometry", "i" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CurveN'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_curvetoline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_curvetoline"("geom" "public"."geometry", "tol" float8, "toltype" int4, "flags" int4);
CREATE OR REPLACE FUNCTION "public"."st_curvetoline"("geom" "public"."geometry", "tol" float8=32, "toltype" int4=0, "flags" int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_CurveToLine'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_delaunaytriangles
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_delaunaytriangles"("g1" "public"."geometry", "tolerance" float8, "flags" int4);
CREATE OR REPLACE FUNCTION "public"."st_delaunaytriangles"("g1" "public"."geometry", "tolerance" float8=0.0, "flags" int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_DelaunayTriangles'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_dfullywithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_dfullywithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dfullywithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_difference
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_difference"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_difference"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8='-1.0'::numeric)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Difference'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_dimension
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dimension"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_dimension"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_dimension'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_disjoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_disjoint"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_disjoint"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'disjoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distance"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_distance"(text, text)
  RETURNS "pg_catalog"."float8" AS $BODY$ SELECT public.ST_Distance($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_distance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_Distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distance"("geog1" "public"."geography", "geog2" "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_distance"("geog1" "public"."geography", "geog2" "public"."geography", "use_spheroid" bool=true)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_distancecpa
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distancecpa"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_distancecpa"("public"."geometry", "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_DistanceCPA'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_distancesphere
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distancesphere"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_distancesphere"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS $BODY$select public.ST_distance( public.geography($1), public.geography($2),false)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_distancesphere
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distancesphere"("geom1" "public"."geometry", "geom2" "public"."geometry", "radius" float8);
CREATE OR REPLACE FUNCTION "public"."st_distancesphere"("geom1" "public"."geometry", "geom2" "public"."geometry", "radius" float8)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_distance_sphere'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_distancespheroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distancespheroid"("geom1" "public"."geometry", "geom2" "public"."geometry", "public"."spheroid");
CREATE OR REPLACE FUNCTION "public"."st_distancespheroid"("geom1" "public"."geometry", "geom2" "public"."geometry", "public"."spheroid")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_distance_ellipsoid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_distancespheroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_distancespheroid"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_distancespheroid"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_distance_ellipsoid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_dump
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dump"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_dump"("public"."geometry")
  RETURNS SETOF "public"."geometry_dump" AS '$libdir/postgis-3', 'LWGEOM_dump'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for st_dumppoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dumppoints"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_dumppoints"("public"."geometry")
  RETURNS SETOF "public"."geometry_dump" AS '$libdir/postgis-3', 'LWGEOM_dumppoints'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000
  ROWS 1000;

-- ----------------------------
-- Function structure for st_dumprings
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dumprings"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_dumprings"("public"."geometry")
  RETURNS SETOF "public"."geometry_dump" AS '$libdir/postgis-3', 'LWGEOM_dump_rings'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for st_dumpsegments
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dumpsegments"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_dumpsegments"("public"."geometry")
  RETURNS SETOF "public"."geometry_dump" AS '$libdir/postgis-3', 'LWGEOM_dumpsegments'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000
  ROWS 1000;

-- ----------------------------
-- Function structure for st_dwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dwithin"(text, text, float8);
CREATE OR REPLACE FUNCTION "public"."st_dwithin"(text, text, float8)
  RETURNS "pg_catalog"."bool" AS $BODY$ SELECT public.ST_DWithin($1::public.geometry, $2::public.geometry, $3);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_dwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dwithin"("geog1" "public"."geography", "geog2" "public"."geography", "tolerance" float8, "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_dwithin"("geog1" "public"."geography", "geog2" "public"."geography", "tolerance" float8, "use_spheroid" bool=true)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_dwithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_dwithin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_dwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_dwithin"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_dwithin'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_endpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_endpoint"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_endpoint"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_endpoint_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_envelope
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_envelope"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_envelope"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_envelope'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_equals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_equals"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_equals"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_Equals'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_estimatedextent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_estimatedextent"(text, text, text);
CREATE OR REPLACE FUNCTION "public"."st_estimatedextent"(text, text, text)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'gserialized_estimated_extent'
  LANGUAGE c STABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_estimatedextent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_estimatedextent"(text, text, text, bool);
CREATE OR REPLACE FUNCTION "public"."st_estimatedextent"(text, text, text, bool)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'gserialized_estimated_extent'
  LANGUAGE c STABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_estimatedextent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_estimatedextent"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_estimatedextent"(text, text)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'gserialized_estimated_extent'
  LANGUAGE c STABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("public"."box3d", float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("public"."box3d", float8)
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("public"."box2d", float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("public"."box2d", float8)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX2D_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("geom" "public"."geometry", "dx" float8, "dy" float8, "dz" float8, "dm" float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("geom" "public"."geometry", "dx" float8, "dy" float8, "dz" float8=0, "dm" float8=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("box" "public"."box3d", "dx" float8, "dy" float8, "dz" float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("box" "public"."box3d", "dx" float8, "dy" float8, "dz" float8=0)
  RETURNS "public"."box3d" AS '$libdir/postgis-3', 'BOX3D_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_expand
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_expand"("box" "public"."box2d", "dx" float8, "dy" float8);
CREATE OR REPLACE FUNCTION "public"."st_expand"("box" "public"."box2d", "dx" float8, "dy" float8)
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX2D_expand'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_exteriorring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_exteriorring"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_exteriorring"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_exteriorring_polygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_filterbym
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_filterbym"("public"."geometry", float8, float8, bool);
CREATE OR REPLACE FUNCTION "public"."st_filterbym"("public"."geometry", float8, float8=NULL::double precision, bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_FilterByM'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_findextent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_findextent"(text, text, text);
CREATE OR REPLACE FUNCTION "public"."st_findextent"(text, text, text)
  RETURNS "public"."box2d" AS $BODY$
DECLARE
	schemaname alias for $1;
	tablename alias for $2;
	columnname alias for $3;
	myrec RECORD;
BEGIN
	FOR myrec IN EXECUTE 'SELECT public.ST_Extent("' || columnname || '") As extent FROM "' || schemaname || '"."' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$BODY$
  LANGUAGE plpgsql STABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_findextent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_findextent"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_findextent"(text, text)
  RETURNS "public"."box2d" AS $BODY$
DECLARE
	tablename alias for $1;
	columnname alias for $2;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT public.ST_Extent("' || columnname || '") As extent FROM "' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$BODY$
  LANGUAGE plpgsql STABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_flipcoordinates
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_flipcoordinates"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_flipcoordinates"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_FlipCoordinates'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_force2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_force2d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_force2d"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_force3d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_force3d"("geom" "public"."geometry", "zvalue" float8);
CREATE OR REPLACE FUNCTION "public"."st_force3d"("geom" "public"."geometry", "zvalue" float8=0.0)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Force3DZ($1, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_force3dm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_force3dm"("geom" "public"."geometry", "mvalue" float8);
CREATE OR REPLACE FUNCTION "public"."st_force3dm"("geom" "public"."geometry", "mvalue" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_3dm'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_force3dz
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_force3dz"("geom" "public"."geometry", "zvalue" float8);
CREATE OR REPLACE FUNCTION "public"."st_force3dz"("geom" "public"."geometry", "zvalue" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_3dz'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_force4d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_force4d"("geom" "public"."geometry", "zvalue" float8, "mvalue" float8);
CREATE OR REPLACE FUNCTION "public"."st_force4d"("geom" "public"."geometry", "zvalue" float8=0.0, "mvalue" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_4d'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_forcecollection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcecollection"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcecollection"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_collection'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_forcecurve
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcecurve"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcecurve"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_curve'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_forcepolygonccw
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcepolygonccw"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcepolygonccw"("public"."geometry")
  RETURNS "public"."geometry" AS $BODY$ SELECT
	  CASE WHEN public.ST_GeometryType($1) IN ('ST_Polygon', 'ST_MultiPolygon')
	  THEN public.ST_Reverse(public.ST_ForcePolygonCW($1))
	  ELSE $1 END $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_forcepolygoncw
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcepolygoncw"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcepolygoncw"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_clockwise_poly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_forcerhr
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcerhr"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcerhr"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_clockwise_poly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_forcesfs
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcesfs"("public"."geometry", "version" text);
CREATE OR REPLACE FUNCTION "public"."st_forcesfs"("public"."geometry", "version" text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_sfs'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_forcesfs
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_forcesfs"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_forcesfs"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_sfs'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_frechetdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_frechetdistance"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_frechetdistance"("geom1" "public"."geometry", "geom2" "public"."geometry", float8='-1'::integer)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_FrechetDistance'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_fromflatgeobuf
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_fromflatgeobuf"(anyelement, bytea);
CREATE OR REPLACE FUNCTION "public"."st_fromflatgeobuf"(anyelement, bytea)
  RETURNS SETOF "pg_catalog"."anyelement" AS '$libdir/postgis-3', 'pgis_fromflatgeobuf'
  LANGUAGE c IMMUTABLE
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for st_fromflatgeobuftotable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_fromflatgeobuftotable"(text, text, bytea);
CREATE OR REPLACE FUNCTION "public"."st_fromflatgeobuftotable"(text, text, bytea)
  RETURNS "pg_catalog"."void" AS '$libdir/postgis-3', 'pgis_tablefromflatgeobuf'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_generatepoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_generatepoints"("area" "public"."geometry", "npoints" int4, "seed" int4);
CREATE OR REPLACE FUNCTION "public"."st_generatepoints"("area" "public"."geometry", "npoints" int4, "seed" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_GeneratePoints'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_generatepoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_generatepoints"("area" "public"."geometry", "npoints" int4);
CREATE OR REPLACE FUNCTION "public"."st_generatepoints"("area" "public"."geometry", "npoints" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_GeneratePoints'
  LANGUAGE c VOLATILE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geogfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geogfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_geogfromtext"(text)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geogfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geogfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_geogfromwkb"(bytea)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_from_binary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geographyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geographyfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_geographyfromtext"(text)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geohash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geohash"("geog" "public"."geography", "maxchars" int4);
CREATE OR REPLACE FUNCTION "public"."st_geohash"("geog" "public"."geography", "maxchars" int4=0)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'ST_GeoHash'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geohash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geohash"("geom" "public"."geometry", "maxchars" int4);
CREATE OR REPLACE FUNCTION "public"."st_geohash"("geom" "public"."geometry", "maxchars" int4=0)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'ST_GeoHash'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomcollfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomcollfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomcollfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE
	WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_GeometryCollection'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomcollfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomcollfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomcollfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE
	WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_GeometryCollection'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomcollfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomcollfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_geomcollfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE
	WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_GeometryCollection'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomcollfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomcollfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomcollfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE
	WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_GeometryCollection'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geometricmedian
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geometricmedian"("g" "public"."geometry", "tolerance" float8, "max_iter" int4, "fail_if_not_converged" bool);
CREATE OR REPLACE FUNCTION "public"."st_geometricmedian"("g" "public"."geometry", "tolerance" float8=NULL::double precision, "max_iter" int4=10000, "fail_if_not_converged" bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_GeometricMedian'
  LANGUAGE c IMMUTABLE
  COST 5000;

-- ----------------------------
-- Function structure for st_geometryfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geometryfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_geometryfromtext"(text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geometryfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geometryfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_geometryfromtext"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geometryn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geometryn"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_geometryn"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_geometryn_collection'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geometrytype
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geometrytype"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_geometrytype"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'geometry_geometrytype'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_geomfromewkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromewkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_geomfromewkb"(bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOMFromEWKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomfromewkt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromewkt"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomfromewkt"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'parse_WKT_lwgeom'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomfromgeohash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgeohash"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgeohash"(text, int4=NULL::integer)
  RETURNS "public"."geometry" AS $BODY$ SELECT CAST(public.ST_Box2dFromGeoHash($1, $2) AS public.geometry); $BODY$
  LANGUAGE sql IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_geomfromgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgeojson"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgeojson"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geom_from_geojson'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgeojson"(json);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgeojson"(json)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_GeomFromGeoJson($1::text)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromgeojson
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgeojson"(jsonb);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgeojson"(jsonb)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_GeomFromGeoJson($1::text)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgml"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgml"(text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geom_from_gml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromgml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromgml"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomfromgml"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public._ST_GeomFromGML($1, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromkml
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromkml"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomfromkml"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geom_from_kml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfrommarc21
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfrommarc21"("marc21xml" text);
CREATE OR REPLACE FUNCTION "public"."st_geomfrommarc21"("marc21xml" text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_GeomFromMARC21'
  LANGUAGE c IMMUTABLE STRICT
  COST 500;

-- ----------------------------
-- Function structure for st_geomfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomfromtext"(text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_geomfromtext"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_geomfromtwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromtwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_geomfromtwkb"(bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOMFromTWKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_geomfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_SetSRID(public.ST_GeomFromWKB($1), $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_geomfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_geomfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_geomfromwkb"(bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_WKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_gmltosql
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_gmltosql"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_gmltosql"(text, int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geom_from_gml'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_gmltosql
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_gmltosql"(text);
CREATE OR REPLACE FUNCTION "public"."st_gmltosql"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public._ST_GeomFromGML($1, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_hasarc
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hasarc"("geometry" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_hasarc"("geometry" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_has_arc'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_hasm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hasm"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_hasm"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_hasm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_hasz
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hasz"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_hasz"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_hasz'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_hausdorffdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hausdorffdistance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_hausdorffdistance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'hausdorffdistance'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_hausdorffdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hausdorffdistance"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_hausdorffdistance"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'hausdorffdistancedensify'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_hexagon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hexagon"("size" float8, "cell_i" int4, "cell_j" int4, "origin" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_hexagon"("size" float8, "cell_i" int4, "cell_j" int4, "origin" "public"."geometry"='010100000000000000000000000000000000000000'::geometry)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Hexagon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_hexagongrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_hexagongrid"("size" float8, "bounds" "public"."geometry", OUT "geom" "public"."geometry", OUT "i" int4, OUT "j" int4);
CREATE OR REPLACE FUNCTION "public"."st_hexagongrid"(IN "size" float8, IN "bounds" "public"."geometry", OUT "geom" "public"."geometry", OUT "i" int4, OUT "j" int4)
  RETURNS SETOF "pg_catalog"."record" AS '$libdir/postgis-3', 'ST_ShapeGrid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for st_interiorringn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_interiorringn"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_interiorringn"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_interiorringn_polygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_interpolatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_interpolatepoint"("line" "public"."geometry", "point" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_interpolatepoint"("line" "public"."geometry", "point" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_InterpolatePoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_intersection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersection"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_intersection"(text, text)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_Intersection($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_intersection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersection"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_intersection"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8='-1'::integer)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Intersection'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_intersection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersection"("public"."geography", "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_intersection"("public"."geography", "public"."geography")
  RETURNS "public"."geography" AS $BODY$SELECT public.geography(public.ST_Transform(public.ST_Intersection(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1, $2)), public.ST_Transform(public.geometry($2), public._ST_BestSRID($1, $2))), public.ST_SRID($1)))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_intersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersects"("geog1" "public"."geography", "geog2" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_intersects"("geog1" "public"."geography", "geog2" "public"."geography")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'geography_intersects'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_intersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersects"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_intersects"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_Intersects'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_intersects
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_intersects"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_intersects"(text, text)
  RETURNS "pg_catalog"."bool" AS $BODY$ SELECT public.ST_Intersects($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_inversetransformpipeline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_inversetransformpipeline"("geom" "public"."geometry", "pipeline" text, "to_srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_inversetransformpipeline"("geom" "public"."geometry", "pipeline" text, "to_srid" int4=0)
  RETURNS "public"."geometry" AS $BODY$SELECT public.postgis_transform_pipeline_geometry($1, $2, FALSE, $3)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isclosed
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isclosed"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isclosed"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_isclosed'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_iscollection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_iscollection"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_iscollection"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_IsCollection'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_isempty
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isempty"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isempty"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_isempty'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_ispolygonccw
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ispolygonccw"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_ispolygonccw"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_IsPolygonCCW'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_ispolygoncw
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ispolygoncw"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_ispolygoncw"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_IsPolygonCW'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_isring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isring"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isring"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'isring'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_issimple
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_issimple"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_issimple"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'issimple'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_isvalid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvalid"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_isvalid"("public"."geometry", int4)
  RETURNS "pg_catalog"."bool" AS $BODY$SELECT (public.ST_isValidDetail($1, $2)).valid$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isvalid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvalid"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isvalid"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'isvalid'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isvaliddetail
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvaliddetail"("geom" "public"."geometry", "flags" int4);
CREATE OR REPLACE FUNCTION "public"."st_isvaliddetail"("geom" "public"."geometry", "flags" int4=0)
  RETURNS "public"."valid_detail" AS '$libdir/postgis-3', 'isvaliddetail'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isvalidreason
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvalidreason"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isvalidreason"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'isvalidreason'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isvalidreason
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvalidreason"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_isvalidreason"("public"."geometry", int4)
  RETURNS "pg_catalog"."text" AS $BODY$
	SELECT CASE WHEN valid THEN 'Valid Geometry' ELSE reason END FROM (
		SELECT (public.ST_isValidDetail($1, $2)).*
	) foo
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_isvalidtrajectory
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_isvalidtrajectory"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_isvalidtrajectory"("public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_IsValidTrajectory'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_largestemptycircle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_largestemptycircle"("geom" "public"."geometry", "tolerance" float8, "boundary" "public"."geometry", OUT "center" "public"."geometry", OUT "nearest" "public"."geometry", OUT "radius" float8);
CREATE OR REPLACE FUNCTION "public"."st_largestemptycircle"(IN "geom" "public"."geometry", IN "tolerance" float8=0.0, IN "boundary" "public"."geometry"='0101000000000000000000F87F000000000000F87F'::geometry, OUT "center" "public"."geometry", OUT "nearest" "public"."geometry", OUT "radius" float8)
  RETURNS "pg_catalog"."record" AS '$libdir/postgis-3', 'ST_LargestEmptyCircle'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_length
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_length"("geog" "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_length"("geog" "public"."geography", "use_spheroid" bool=true)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_length'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_length
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_length"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_length"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_length2d_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_length
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_length"(text);
CREATE OR REPLACE FUNCTION "public"."st_length"(text)
  RETURNS "pg_catalog"."float8" AS $BODY$ SELECT public.ST_Length($1::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_length2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_length2d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_length2d"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_length2d_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_length2dspheroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_length2dspheroid"("public"."geometry", "public"."spheroid");
CREATE OR REPLACE FUNCTION "public"."st_length2dspheroid"("public"."geometry", "public"."spheroid")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_length2d_ellipsoid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_lengthspheroid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lengthspheroid"("public"."geometry", "public"."spheroid");
CREATE OR REPLACE FUNCTION "public"."st_lengthspheroid"("public"."geometry", "public"."spheroid")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_length_ellipsoid_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_letters
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_letters"("letters" text, "font" json);
CREATE OR REPLACE FUNCTION "public"."st_letters"("letters" text, "font" json=NULL::json)
  RETURNS "public"."geometry" AS $BODY$
DECLARE
  letterarray text[];
  letter text;
  geom geometry;
  prevgeom geometry = NULL;
  adjustment float8 = 0.0;
  position float8 = 0.0;
  text_height float8 = 100.0;
  width float8;
  m_width float8;
  spacing float8;
  dist float8;
  wordarr geometry[];
  wordgeom geometry;
  -- geometry has been run through replace(encode(st_astwkb(geom),'base64'), E'\n', '')
  font_default_height float8 = 1000.0;
  font_default json = '{
  "!":"BgACAQhUrgsTFOQCABQAExELiwi5AgAJiggBYQmJCgAOAg4CDAIOBAoEDAYKBgoGCggICAgICAgGCgYKBgoGCgQMBAoECgQMAgoADAIKAAoADAEKAAwBCgMKAQwDCgMKAwoFCAUKBwgHBgcIBwYJBgkECwYJBAsCDQILAg0CDQANAQ0BCwELAwsDCwUJBQkFCQcHBwcHBwcFCQUJBQkFCQMLAwkDCQMLAQkACwEJAAkACwIJAAsCCQQJAgsECQQJBAkGBwYJCAcIBQgHCAUKBQoDDAUKAQwDDgEMAQ4BDg==",
  "&":"BgABAskBygP+BowEAACZAmcAANsCAw0FDwUNBQ0FDQcLBw0HCwcLCQsJCwkLCQkJCwsJCwkLCQ0HCwcNBw8HDQUPBQ8DDwMRAw8DEQERAREBEQERABcAFQIXAhUCEwQVBBMGEwYTBhEIEQgPChEKDwoPDA0MDQwNDgsOCRAJEAkQBxAHEgUSBRQFFAMUAxQBFgEWARgAigEAFAISABICEgQQAhAEEAQQBg4GEAoOCg4MDg4ODgwSDgsMCwoJDAcMBwwFDgUMAw4DDgEOARABDgEQARIBEAASAHgAIAQeBB4GHAgaChoMGA4WDhYQFBISEhISDhQQFAwWDBYKFgoYBhgIGAQYBBgCGgAaABgBGAMYAxYHFgUWCRYJFAsUCxIPEg0SERARDhMOFQwVDBcIGQYbBhsCHQIfAR+dAgAADAAKAQoBCgEIAwgFBgUGBQYHBAUEBwQHAgcCBwIHAAcABwAHAQcBBwMHAwUDBwUFBQUHBQUBBwMJAQkBCQAJAJcBAAUCBQAFAgUEBQIDBAUEAwQDBgMEAQYDBgEGAAgBBgAKSeECAJ8BFi84HUQDQCAAmAKNAQAvExMx",
  "\"":"BgACAQUmwguEAgAAkwSDAgAAlAQBBfACAIACAACTBP8BAACUBA==",
  "''":"BgABAQUmwguEAgAAkwSDAgAAlAQ=",
  "(":"BgABAUOQBNwLDScNKw0rCysLLwsxCTEJMwc1BzcHNwM7AzsDPwE/AEEANwI1AjMEMwIzBjEGLwYvCC0ILQgrCCkKKQonCicMJbkCAAkqCSoHLAksBywFLgcuBS4FMAMwAzADMgEwATQBMgA0ADwCOgI6BDoEOAY4BjYINgg2CjQKMgoyCjIMMAwwDi7AAgA=",
  ")":"BgABAUMQ3Au6AgAOLQwvDC8KMQoxCjEKMwg1CDUGNQY3BDcEOQI5AjkAOwAzATEBMQExAy8DLwMvBS8FLQctBS0HKwktBykJKwkpswIADCYKKAooCioIKggsCC4ILgYwBjAGMgQ0AjQCNAI2ADgAQgFAAz4DPAM8BzgHOAc2CTQJMgsyCzALLg0sDSoNKg==",
  "+":"BgABAQ3IBOwGALcBuAEAANUBtwEAALcB0wEAALgBtwEAANYBuAEAALgB1AEA",
  "/":"BgABAQVCAoIDwAuyAgCFA78LrQIA",
  "4":"BgABAhDkBr4EkgEAEREApwJ/AADxARIR5QIAEhIA9AHdAwAA7ALIA9AG6gIAEREA8QYFqwIAAIIDwwH/AgABxAEA",
  "v":"BgABASDmA5AEPu4CROwBExb6AgAZFdMC0wgUFaECABIU0wLWCBcW+AIAExVE6wEEFQQXBBUEFwQVBBUEFwQVBBUEFwQVBBUEFwQXBBUEFwYA",
  ",":"BgABAWMYpAEADgIOAgwCDgQMBAoGDAYKBgoICAgICAgICAoGCgYKBAoEDAQKBAoCDAIKAgwCCgAKAAwACgEMAQoBCgMMAwoDCgUKBQgFCgUIBwYJCAcGCQYJBAsGCQQLAg0CCwINAg0AAwABAAMAAwADAQMAAwADAAMBBQAFAQcBBwEHAwcBCQMJAQsDCwMLAw0FDQMNBQ8FDwURBxMFEwkTBxcJFwkXswEAIMgBCQYJBgkGBwYJCAcIBQgHCgUKBQoFDAEMAwwBDgEOABA=",
  "-":"BgABAQUq0AMArALEBAAAqwLDBAA=",
  ".":"BgABAWFOrAEADgIOAg4CDgQMBAoGDAYKBgoICAgKCAgIBgoGCgYKBgoEDAQKBAwECgIMAAwCDAAMAAwBCgAMAQoDDAMKAwoDCgUKBQgFCgUIBwgJBgcICQYJBgsGCQQLAg0CDQINAA0ADQENAQ0BCwMNAwkFCwUJBQkHBwcJBwUHBwkFCQUJBQkDCwMJAwsDCQELAAsBCwALAAsCCQALAgkECwQJBAkECQYJBgcGBwgJBgcKBQgHCgUKBQwFCgEOAwwBDgEOAA4=",
  "0":"BgABAoMB+APaCxwAHAEaARoDFgMYBRYFFAcUBxIJEgkQCRALEAsOCwwNDA0MDQoPCg0IDwgPBhEGDwYRBA8EEQIRAhMCEQITABMA4QUAEQETAREBEQMRAxEFEQURBREHDwkPBw8JDwsNCw0LDQ0NDQsNCw8JEQkRCREJEwcTBxUFFQUVAxUDFwEXARkAGQAZAhcCFwQXBBUGEwYTCBMIEQoRCg8KDwoPDA0MDQ4NDgsOCQ4JEAkQBxAHEAUSBRIDEgMSAxIDEgESARQAEgDiBQASAhQCEgISBBIEEgYSBhIGEggQChAIEAoQDBAMDgwODg4ODA4MEgwQChIKEggUCBQIFgYWBBYGGAQYAhgCGgILZIcDHTZBEkMRHTUA4QUeOUITRBIePADiBQ==",
  "2":"BgABAWpUwALUA44GAAoBCAEKAQgDBgMGBQYFBgUEBwQFBAUCBwIHAgUABwAHAAUBBwMFAQcFBQMHBQUHBQcFBwMJAwkBCQELAQsAC68CAAAUAhIAFAISBBQCEgQUBBIEEgYUCBIGEAgSChAKEAoQDBAMDg4ODgwQDBIMEgoSChQIFggWCBgGGAQaAhwCHAIWABQBFgEUARQDFAMSAxQFEgUSBxIHEAkQCRALDgsODQ4NDA8KDwwRCBMKEwgTBhUGFwQXBBcEGwAbABsAHQEftwPJBdIDAACpAhIPzwYAFBIArgI=",
  "1":"BgABARCsBLALAJ0LEhERADcA2QEANwATABQSAOYIpwEAALgCERKEBAASABER",
  "3":"BgABAZ0B/gbEC/sB0QQOAwwBDAMMAwwFCgMKBQoFCgUIBwoFCAcICQgJBgkICQYLCAsECwYLBA0GDwINBA8CDwQRAhECEQITABUCFQAVAH0AEQETAREBEQETAxEDEQURBREFDwcRBw8JDwkNCQ8LDQsNDQsNCw0LDwsPCREJEQcRBxMFFQUVBRUDFwEXARkAGQAZAhkCFwQVBBUEEwYTCBEIEQgRCg0MDwoNDA0OCw4LDgkQCRAHEAkQBRAFEgUSAxIDFAMSAxYBFAEWARYAFqQCAAALAgkCCQQHAgcGBwYHBgUIBQYDCAMIAwYDCAEIAQgACAAIAAgCCAIIAgYCCAQIBAgGBgYEBgQIBAoCCgAKAAwAvAEABgEIAAYBBgMGAwQDBgMEBQQDBAUCBQQFAgUABwIFAJkBAACmAaIB3ALbAgAREQDmAhIRggYA",
  "5":"BgABAaAB0APgBxIAFAESABIBEgMSARADEgMQAxIFEAcOBRAHDgkOCQ4JDgsMCwwLCgsKDQoPCA0IDwgPBhEEEwYTAhMEFwIXABcAiQIAEwETABEBEQMTAxEDDwMRBQ8FDwUPBw8JDQcNCQ0LDQsLCwsNCw0JDwkPCREHEQcTBxMFEwMVAxcDGQEZARkAFwAVAhUCFQQTBBMGEwYRCBEIDwoPCg8KDQwNDA0MCw4LDgkOCRAJEAcOBxAHEgUQBRIDEAMSAxIBEgEUARIAFLgCAAAFAgUABQIFBAUCBQQDBAUEAwYDBgMIAwgBCAEIAQoACAAIAgYACAQGAgQEBgQEBAQGBAQCBgIGAgYCBgIIAAYA4AEABgEIAAYBBgMGAQQDBgMEAwQFBAMCBQQFAgUABwIFAPkBAG+OAQCCBRESAgAAAuYFABMRAK8CjQMAAJ8BNgA=",
  "7":"BgABAQrQBsILhQOvCxQR7wIAEhK+AvYIiwMAAKgCERKwBgA=",
  "6":"BgABAsYBnAOqBxgGFgYYBBYEFgIWABQBFgEUAxQDFAUUBRIFEAcSCRAJEAkOCw4NDgsMDQoPCg8KDwgRCBEGEQYRBBMCEwITAhUAkwIBAAERAREBEQEPAxEFEQMPBREFDwcPBw8HDwkNCQ0LDQsNCwsNCw0LDQkPCQ8JDwcRBxEHEwUTAxMFFQEXAxcBGQAVABUCEwIVBBMEEQYTBhEIEQgPChEKDQoPDA0MDQwNDgsOCxALDgkQCRAHEgcQBxIFEgUSBRIBFAMSARIBFAASAOIFABACEgIQAhIEEAQQBhIGEAYQCBAKEAgOChAMDgwMDA4ODA4MDgwODBAKEAoQChIIEggSBhQGFgYUAhYCGAIYABoAGAEYARYBFgMUBRQFEgUSBxAHEAcQCQ4LDgkMCwwNDA0KDQgPCg0GEQgPBhEEEQQRBBMEEwITAhMCFQIVABWrAgAACgEIAQoBCAEGAwYDBgUGBQQFBAUEBQQFAgUABwIFAAUABwEFAAUBBQMFAwUDBQMFBQMFAwUBBQEHAQkBBwAJAJcBDUbpBDASFi4A4AETLC8SBQAvERUrAN8BFC0yEQQA",
  "8":"BgABA9gB6gPYCxYAFAEUARYBEgMUBRQFEgUSBxIHEAcSCQ4JEAkOCw4LDgsMDQwNCg0KDQoPCg8IDwgPBhEGEQQPBBMCEQIRABMAQwAxAA8BEQEPAREDDwMRAw8FEQUPBxEJDwkPCQ8NDw0PDQ8IBwYHCAcGBwgHBgkGBwYJBgcECQYJBAkGCQQJBAsECwQLBA0CCwINAg8CDwIPAA8AaQATAREBEwERAxEFEQURBREHEQcPBw8JDwkPCw8LDQsNDQ0LCw0LDwsNCQ8JDwcPBw8HEQURAxEFEQMRARMBEwFDABEAEwIRAhEEEQQRBg8GEQgPCA8KDwoPCg0MDQwNDAsOCw4LDgkQCRAJDgkQBxIHEAcSBRADEgMUAxIBFAEUABQAagAOAhAADgIOAg4EDAIOBAwEDAQMBgwECgYMBAoGCAYKBgoGCggKBgoICgYICAoICA0MCwwLDgsOCRAHEAcQBxIFEgUSAxIDEgMSARABEgASADIARAASAhICEgQSAhIGEAYSBhAIEAgQCBAKDgoODA4MDgwMDgwODA4KEAwQCBIKEggSCBQIFAYUBBQEFgQWAhYCGAANT78EFis0EwYANBIYLgC0ARcsMRQFADERGS0AswELogHtAhcuNxA3DRkvALMBGjE6ETYSGDIAtAE=",
  "9":"BgABAsYBpASeBBcFFQUXAxUDFQEVABMCFQITBBMEEwYRBhMGDwgRCg8KDwoNDA0OCwwNDgkQCRAJEAcSBxIFEgUSAxQBFAEUARYAlAICAAISAhICEgQSAhAGEgQQBhIGEAgSCA4IEAoOChAMDAwODAwODA4MEAoOChAKEAgSCBIIFAYUBBQGFgIYBBgCGgAWABYBFAEWAxQDEgUUBRIHEgcQCRIJEAkOCw4LDgsODQwNDA0MDwoPCg8IDwgRCBEGEQYRBhEEEQITAhECEwARAOEFAA8BEQEPAREDDwMPBREFDwUPBw8JDwcNCQ8LDQsLCw0NCw0LDQsNCw8JEQkPCREHEQcTBRMFEwUTARUBFQEXABkAFwIXAhcCFQQTBhMGEQYRCA8IDwgNCg8MCwoLDAsOCQ4JDgkQBxAHEAUQBRIFEgMSAxQDFAEUAxQAFgEWABamAgAACwIJAgkCCQIHBAcEBwYFBgUGAwYDBgMGAQgBBgEIAAgABgIIAgYCBgQGBAYEBgYGBgQIBAgECAIKAgoCCgAMAJgBDUXqBC8RFS0A3wEUKzARBgAwEhYsAOABEy4xEgMA",
  ":":"BgACAWE0rAEADgIOAg4CDgQMBAoGDAYKBgoICAgKCAgIBgoGCgYKBgoEDAQKBAwECgIMAAwCDAAMAAwBCgAMAQoDDAMKAwoDCgUKBQgFCgUIBwgJBgcICQYJBgsGCQQLAg0CDQINAA0ADQENAQ0BCwMNAwkFCwUJBQkHBwcJBwUHBwkFCQUJBQkDCwMJAwsDCQELAAsBCwALAAsCCQALAgkECwQJBAkECQYJBgcGBwgJBgcKBQgHCgUKBQwFCgEOAwwBDgEOAA4BYQDqBAAOAg4CDgIOBAwECgYMBgoGCggICAoICAgGCgYKBgoGCgQMBAoEDAQKAgwADAIMAAwADAEKAAwBCgMMAwoDCgMKBQoFCAUKBQgHCAkGBwgJBgkGCwYJBAsCDQINAg0ADQANAQ0BDQELAw0DCQULBQkFCQcHBwkHBQcHCQUJBQkFCQMLAwkDCwEJAwsACwELAAsACwIJAAsECQILBAkECQQJBgkGBwYHCAkGBwoFCAcKBQoFDAUKAQ4DDAEOAQ4ADg==",
  "x":"BgABARHmAoAJMIMBNLUBNrYBMIQB1AIA9QG/BI4CvwTVAgA5hgFBwAFFxwE1fdUCAI4CwATzAcAE1AIA",
  ";":"BgACAWEslgYADgIOAg4CDgQMBAoGDAYKBgoICAgKCAgIBgoGCgYKBgoEDAQKBAwECgIMAAwCDAAMAAwBCgAMAQoDDAMKAwoDCgUKBQgFCgUIBwgJBgcICQYJBgsGCQQLAg0CDQINAA0ADQENAQ0BCwMNAwkFCwUJBQkHBwcJBwUHBwkFCQUJBQkDCwMJAwsBCQMLAAsBCwALAAsCCQALBAkCCwQJBAkECQYJBgcGBwgJBgcKBQgHCgUKBQwFCgEOAwwBDgEOAA4BYwjxBAAOAg4CDAIOBAwECgYMBgoGCggICAgICAgICgYKBgoECgQMBAoECgIMAgoCDAIKAAoADAAKAQwBCgEKAwwDCgMKBQoFCAUKBQgHBgkIBwYJBgkECwYJBAsCDQILAg0CDQADAAEAAwADAAMBAwADAAMAAwEFAAUBBwEHAQcDBwEJAwkBCwMLAwsDDQUNAw0FDwUPBREHEwUTCRMHFwkXCRezAQAgyAEJBgkGCQYHBgkIBwgFCAcKBQoFCgUMAQwDDAEOAQ4AEA==",
  "=":"BgACAQUawAUA5gHEBAAA5QHDBAABBQC5AgDsAcQEAADrAcMEAA==",
  "B":"BgABA2e2BMQLFgAUARQBFAEUAxIDEgUSBRIFEAcQBxAJDgkOCQ4LDgsMCwwNDA0KDQgNCg0IDwYPBg8GDwQRBBEEEQIRAhMAEwAHAAkABwEHAAkBCQAHAQkBCQEHAQkBCQMJAwcDCQMJAwkFBwUJAwkHCQUHBQkHCQcJBwcHBwkHBwcJBwsHCQUQBQ4FDgcOCQ4JDAkMCwoNCg0IDwgRBhMEFQQXAhcCGwDJAQEvAysFJwklDSMPHREbFRkXFRsTHw8fCyUJJwcrAy0B6wMAEhIAoAsREuYDAAiRAYEElgEAKioSSA1EOR6JAQAA0wEJkAGPBSwSEiwAzAETKikSjwEAAMUCkAEA",
  "A":"BgABAg/KBfIBqQIAN98BEhHzAgAWEuwCngsREvwCABMR8gKdCxIR8QIAFBI54AEFlwGCBk3TA6ABAE3UAwMA",
  "?":"BgACAe4BsgaYCAAZABkBFwEXBRUDEwUTBxEHEQcPCQ8JDQkNCQ0LCwsLCwsLCQsJCwcNBwsHDQcLBQsFDQULAwkFCwMLAwkDCQMBAAABAQABAAEBAQABAAEAAQABAAABAQAAAQEAEwcBAQABAAMBAwADAAUABQAFAAcABwAFAAcABwAFAgcABQAHAAUAW7cCAABcABgBFgAUAhQAFAISAhACEAIQBA4EDgQMBgwGDAYMBgoICgYKCAgKCggICAgKBgoICgYMCAwGDAgOBg4GEAYQBgIAAgIEAAICBAACAgQCBAIKBAoGCAQKBggIBgYICAYIBggGCgQIBAoECAQKAggCCgIKAAgACgAKAAgBCAEKAwgDCAMIAwgFBgMIBQYHBAUGBQQFBAcCBQQHAgcCCQIHAgkCBwAJAgkACQAJAAkBCQAJAQsACQELAQsDCwELAwsDCwMLAwsDCwULAwsFCwMLBV2YAgYECAQKBAwGDAQMBhAIEAYSBhIIEgYUBhIEFgYUBBYEFgQWAhgCFgIYABYAGAAYARgBGAMWBRYHFgcWCRYLFA0IBQYDCAUIBwYFCAcGBwgHBgcICQYJCAkGCQYJCAsGCwYLBgsGDQYNBA0GDQQNBA8EDwQPAg8EEQIRAhEAEQITAWGpBesGAA4CDgIOAg4EDAQKBgwGCgYKCAgICggICAYKBgoGCgYKBAwECgQMBAoCDAAMAgwADAAMAQoADAEKAwwDCgMKAwoFCgUIBQoFCAcICQYHCAkGCQYLBgkECwINAg0CDQANAA0BDQENAQsDDQMJBQsFCQUJBwcHCQcFBwcJBQkFCQUJAwsDCQMLAwkBCwALAQsACwALAgkACwIJBAsECQQJBAkGCQYHBgcICQYHCgUIBwoFCgUMBQoBDgMMAQ4BDgAO",
  "C":"BgABAWmmA4ADAAUCBQAFAgUEBQIDBAUEAwQDBgMEAQYDBgEGAAgBBgDWAgAAwQLVAgATABMCEQITBBEEEQQRBhEIEQgPCA8KDwoNCg0MDQwNDAsOCw4LDgkOCxAHEAkQBxIHEgUSBRIDEgEUARIBFAAUAMIFABQCFAISBBQEEgQSBhIIEggSCBAKEAoQCg4MDgwODA4ODA4MDgwQDA4KEggQChIIEggSBhIGFAQSAhQCEgIUAMYCAADBAsUCAAUABwEFAAUBBQMDAQUDAwMDAwMFAQMDBQEFAAUBBwAFAMEF",
  "L":"BgABAQmcBhISEdkFABIQALQLwgIAAIEJ9AIAAK8C",
  "D":"BgABAkeyBMQLFAAUARIBFAESAxIDEgMSBRIFEAcQBxAHDgkOCQ4LDgsMCwwNDA0KDwoPCg8IDwgRCBEGEwQTBBMEEwIVAhUAFwDBBQAXARcBFwMTAxUDEwUTBxEHEQcPCQ8JDwkNCw0LCwsLDQsNCQ0JDQcPBw8HDwcRBREFEQMRAxEDEwERARMBEwDfAwASEgCgCxES4AMACT6BAxEuKxKLAQAAvwaMAQAsEhIsAMIF",
  "F":"BgABARGABoIJ2QIAAIECsgIAEhIA4QIRErECAACvBBIR5QIAEhIAsgucBQASEgDlAhES",
  "E":"BgABARRkxAuWBQAQEgDlAhES0QIAAP0BtgIAEhIA5wIRFLUCAAD/AfACABISAOUCERLDBQASEgCyCw==",
  "G":"BgABAZsBjgeIAgMNBQ8FDQUNBQ0HCwcNBwsHCwkLCQsJCwsJCwsLCQsJDQkLBw0HDwcNBw8FDwUPAw8DEQMPAxEBEQERARMBEQAXABUCFwIVAhMEFQQTBhMGEwYRCBEIDwoRCg8KDwwNDA0MDQ4LDgkQCRAJEAcQBxIFEgUUBRQDFAMUARYBFgEYAMoFABQCFAASBBQCEgQSBBIEEgYSBhAGEAgQCBAKDgoOCg4MDgwMDgwOChAKEAoSCBIIFAgUBhQEGAYWAhgEGAIaAOoCAAC3AukCAAcABwEFAQUBBQMFAwMFAwUDBQEFAQcBBQEFAQUABwAFAMUFAAUCBwIFAgUCBQQFBAMGBQYDBgUGAwgDBgMIAQgDCAEIAQoBCAEIAAgACgAIAAgCCAIIAggECgQGBAgECAYIBgC6AnEAAJwCmAMAAJcF",
  "H":"BgABARbSB7ILAQAAnwsSEeUCABISAOAE5QEAAN8EEhHlAgASEgCiCxEQ5gIAEREA/QPmAQAAgAQPEOYCABER",
  "I":"BgABAQmuA7ILAJ8LFBHtAgAUEgCgCxMS7gIAExE=",
  "J":"BgABAWuqB7ILALEIABEBEwERAREDEwMRAxEFEQURBw8HEQcPCQ0LDwsNCw0NDQ0LDwsPCxEJEQkTCRMJFQcVBxcFFwMZAxsBGwEbAB8AHQIbAhsEGQYXBhcGFQgTCBMKEwoRDA8KDwwNDA0OCw4LDgkQCRAJEAcQBRIFEgUSAxQDEgESARIBFAESABIAgAEREtoCABERAn8ACQIHBAcEBwYHBgUIBQoDCgMKAwoDDAEKAQwBCgEMAAwACgAMAgoCDAIKBAoECgYKBggGBgYGCAQGBAgCCgAIALIIERLmAgAREQ==",
  "M":"BgACAQRm1gsUABMAAAABE5wIAQDBCxIR5QIAEhIA6gIK5gLVAe0B1wHuAQztAgDhAhIR5QIAEhIAxAsUAPoDtwT4A7YEFgA=",
  "K":"BgABAVXMCRoLBQsDCQMLAwsDCwMLAwsBCwELAQsBCwELAQ0ACwELAAsADQALAg0ACwILAA0CCwILAgsCDQQLBAsECwYNBAsGCwYLCAsGCwgJCgsICQoJCgkMCQwJDAkOCRALEAkQCRKZAdICUQAAiwQSEecCABQSAKALExLoAgAREQC3BEIA+AG4BAEAERKCAwAREdkCzQXGAYUDCA0KDQgJCgkMBwoFDAUMAQwBDgAMAg4CDAQOBAwGDghmlQI=",
  "O":"BgABAoMBsATaCxwAHAEaARoDGgMYBRYFFgcWBxQJEgkSCRILEAsODQ4NDg0MDwoNDA8KDwgPCBEIDwYRBg8GEQQRAhMCEQITABMA0QUAEQETAREBEQMTBREFEQURBxEHDwcRCQ8LDQsPCw0NDQ0NDwsPCw8LEQkTCRMJEwkVBxUHFwUXAxkDGQEbARsAGwAZAhkCGQQXBhcGFQYVCBUIEwoRChEMEQoRDA8MDQ4NDg0OCxAJEAsQCRAHEgcSBxIFFAMSAxIDEgEUARIAEgDSBQASAhQCEgISBBIEEgYSBhIIEggQCBAKEgwODBAMEA4ODg4QDhIMEAwSChQKFAgUCBYIFgYYBBoGGgQcAh4CHgILggGLAylCWxZbFSlBANEFKklcGVwYKkwA0gU=",
  "N":"BgABAQ+YA/oEAOUEEhHVAgASEgC+CxQAwATnBQDIBRMS2AIAExEAzQsRAL8ElgU=",
  "P":"BgABAkqoB5AGABcBFQEVAxMDEwMTBREHEQcRBw8JDwkNCQ0LDQsNCwsNCw0JDQkNCQ8HDwcPBxEFEQURAxEDEQMTAREBEwETAH8AAIMDEhHlAgASEgCgCxES1AMAFAAUARIAFAESAxIDEgMSAxIFEAUQBRAHDgkOCQ4JDgsMCwwNDA0KDQoNCg8IDwgRCBEGEwQTBBUEFQIXAhkAGQCzAgnBAsoCESwrEn8AANUDgAEALBISLgDYAg==",
  "R":"BgABAj9msgsREvYDABQAFAESARQBEgESAxIDEgUSBRAFEAcQBw4JDgkOCQ4LDAsMDQwLCg0KDwoNCA8IDwgPBhEEEwYTAhMEFQIXABcAowIAEwEVARMDEwMTBRMFEQcTBxELEQsRDQ8PDREPEQ0VC8QB/QMSEfkCABQSiQGyA3EAALEDFBHnAgASEgCgCwnCAscFogEALhISLACqAhEsLRKhAQAApQM=",
  "Q":"BgABA4YBvAniAbkB8wGZAYABBQUFAwUFBQUHBQUDBwUFBQcFBQMHBQcDBwUJAwcDCQMJAwkDCQMJAQsDCwMLAQsDCwENAw0BDQEPAA8BDwAPABsAGwIZAhcEGQQXBBUGFQgVCBMIEQoTChEKDwwPDA8ODQ4NDgsQCxAJEAkQBxIHEgUSBRQFFAMUARQDFAEWABYAxgUAEgIUAhICEgQSBBIGEgYSCBIIEAgQChIMDgwQDBAODg4OEA4SDBAMEgoUChQIFAgWCBYGGAQaBhoEHAIeAh4CHAAcARoBGgMaAxgFFgUWBxYHFAkSCRIJEgsQCw4NDg0ODQwPCg0MDwoPCA8IEQgPBhEGDwYRBBECEwIRAhMAEwC7BdgBrwEImQSyAwC6AylAWxZbFSk/AP0BjAK7AQeLAoMCGEc4J0wHVBbvAaYBAEM=",
  "S":"BgABAYMC8gOEBxIFEgUQBxIFEgcSBxIJEgcSCRIJEAkQCRALEAsOCw4NDg0MDQ4PDA0KEQoPChEKEQgRCBMGFQQTBBcCFQAXABkBEwARAREBEQMPAQ8DDwMPAw0DDQUNAw0FCwULBwsFCwUJBwsFCQcHBQkHCQUHBwcHBwUHBwUFBQcHBwUHAwcFEQsRCxMJEwkTBxMFEwUVBRUDFQMVARMBFwEVABUAFQIVAhUCFQQVBBUEEwYVBhMIEwgTCBMIEwgRCBMKEQgRCmK6AgwFDgUMAw4FEAUOBRAFEAUQBRAFEAMSAw4DEAMQAxABEAEOAQ4AEAIMAg4CDgQMBAwGCggKCAoKBgwGDgYQBBACCgAMAAoBCAMKBQgFCAcIBwgJCAsGCQgLCA0IDQgNCA8IDQgPCA8IDwgPChEIDwgPCBEKDwoPDBEMDwwPDg8ODw4NEA0QCxALEgsSCRIHEgcUBRQFGAUYAxgBGgEcAR4CJAYkBiAIIAweDBwQHBAYEhgUFBYUFhQWEBoQGg4aDBwKHAoeBh4GIAQgAiACIgEiASIFIgUiBSAJIgkgCyINZ58CBwQJAgkECwQLAgsECwINBA0CDQQNAg0CDQALAg0ADQANAAsBCwELAQsDCwULBQkFCQcHBwcJBwkFCwMLAw0BDQENAAsCCwQLBAkGCQgJCAkKBwoJCgcMBQoHDAcMBQwF",
  "V":"BgABARG2BM4DXrYEbKwDERL0AgAVEesCnQsSEfsCABQS8QKeCxES8gIAExFuqwNgtQQEAA==",
  "T":"BgABAQskxAv0BgAAtQKVAgAA+wgSEeUCABISAPwImwIAALYC",
  "U":"BgABAW76B7ALAKMIABcBFwMXARUFFQUTBxMHEwkRCREJEQsPDQ0LDw0NDwsPCw8LEQkPCRMJEQcTBxMFEwUVBRUDEwMXARUBFQEXABUAEwIVAhMCFQQTBBUEEwYTBhMIEwgRChEIEQwRDA8MDw4PDg0OCxANEAsSCRIJEgcUBxQHFAMWBRYBGAEYARgApggBAREU9AIAExMAAgClCAALAgkECQQHBAcIBwgHCAUKBQoDCgMKAwwBCgEMAQwADAAMAgoCDAIKAgoECgQKBggGCAYICAYKBAgCCgIMAgwApggAARMU9AIAExM=",
  "X":"BgABARmsCBISEYkDABQSS54BWYICXYkCRZUBEhGJAwAUEtYCzgXVAtIFExKIAwATEVClAVj3AVb0AVKqAREShgMAERHXAtEF2ALNBQ==",
  "W":"BgABARuODcQLERHpAp8LFBHlAgASEnW8A2+7AxIR6wIAFBKNA6ALERKSAwATEdQB7wZigARZ8AIREugCAA8RaKsDYsMDXsoDaqYDExLqAgA=",
  "Y":"BgABARK4BcQLhgMAERHnAvMGAKsEEhHnAgAUEgCsBOkC9AYREoYDABERWOEBUJsCUqICVtwBERI=",
  "Z":"BgABAQmAB8QLnwOBCaADAADBAusGAMgDggmhAwAAwgLGBgA=",
  "`":"BgABAQfqAd4JkQHmAQAOlgJCiAGpAgALiwIA",
  "c":"BgABAW3UA84GBQAFAQUABQEFAwMBBQMDAwMDAwUBAwMFAQUABQEHAAUAnQMABQIFAAUCBQQFAgMEBQQDBAMGAwQBBgMGAQYABgEGAPABABoMAMsCGw7tAQATABMCEwARAhMEEQIPBBEEDwQPBg8IDwYNCA0KDQoNCgsMCwwLDAkOCRAHDgcQBxIFEgUUBRQDFAEWAxgBGAAYAKQDABQCFAISBBQCEgYSBhAGEggQCBAIEAoQCg4MDAwODAwODAwKDgwQCg4IEAgQCBAIEAYSBhIGEgQSAhQCFAIUAOABABwOAM0CGQzbAQA=",
  "a":"BgABApoB8AYCxwF+BwkHCQcJCQkHBwkHBwcJBQkFBwUJBQkFCQMHBQkDCQMJAwcDCQEHAQkBBwEJAQcABwAHAQcABQAHAAUBBQAFABMAEwITAhEEEwQPBBEGDwgPCA0IDwoLCg0KCwwLDAsMCQ4JDgkOBw4HEAcQBRAFEAUSAxADEgESAxIBFAESABQAFAISAhQCEgQSBBIEEgYSBhIIEAgQChAIDgwODA4MDg4MDgwODBAMEAoSCBIKEggUCBQGFgYWBBgEGAIaAhoAcgAADgEMAQoBCgEIAwgDBgUEBQQFBAcCBwIHAgkCCQAJAKsCABcPAMwCHAvCAgAUABYBEgAUARIDFAMQAxIDEAUSBQ4FEAcOCRAJDAkOCwwLDA0MCwoNCg8IDwgPCA8GEQYRBhMEEwIXAhUCFwAZAIMGFwAKmQLqA38ATxchQwgnGiMwD1AMUDYAdg==",
  "b":"BgABAkqmBIIJGAAYARYBFgEUAxQDEgUSBRIFEAcQCQ4HDgkOCw4LDAsMDQoNCg0KDQgPBg8GDwYRBBEEEQQTBBECEwIVAhMAFQD/AgAZARcBFwEXAxUDEwUTBREFEQcPBw8JDwkNCQ0LDQsLCwsNCQ0JDQcPBw8HDwURAxEDEQMTAxMBEwMVARUAFQHPAwAUEgCWCxEY5gIAERkAowKCAQAJOvECESwrEn8AAJsEgAEALBISLgCeAw==",
  "d":"BgABAkryBgDLAXAREQ8NEQ0PDREJDwkRBw8FDwURAw8DDwERAw8BEQEPACMCHwQfCB0MGw4bEhcUFxgVGhEeDSANJAkmBSgDKgEuAIADABYCFAIUAhQCFAQUBBIGEgYSBhAIEAgQCBAKDgoODAwMDAwMDgoOCg4KEAgQCBIGEgYSBhQEFgQWBBYCGAIYAHwAAKQCERrmAgARFwCnCxcADOsCugJGMgDmA3sAKxERLQCfAwolHBUmBSQKBAA=",
  "e":"BgABAqMBigP+AgAJAgkCCQQHBAcGBwYFCAUIBQgDCgMIAQoDCAEKAQoACgAKAAoCCAIKAggECgQIBAgGCAYGBgQIBAoECAIKAAyiAgAAGQEXARcBFwMVBRMFEwURBxEHDwcPCQ8LDQkNCwsNCw0LDQkNBw8JDwcPBQ8FEQURAxEDEwMTAxMBFQAVARcALwIrBCkIJwwlDiESHxQbGBkaFR4TIA0iCyQJKAMqASwAggMAFAIUABIEFAISBBIEEgQSBhIGEAgQCBAIEAoODA4MDgwODgwQDBAKEAoSChIIFAgUCBYGGAQYBhoCGgQcAh4ALgEqAygFJgkkDSANHhEaFRgXFBsSHQ4fDCUIJwQpAi0AGQEXAxcDFQcTBRMJEQkPCw8LDQ0PDQsNDQ8LEQsRCxEJEwkTCRMJEwcTBxUHFQUVBRUHFQUVBRUHFwcVBRUHCs4BkAMfOEUURxEfMwBvbBhAGBwaBiA=",
  "h":"BgABAUHYBJAGAAYBBgAGAQYDBgEEAwYDBAMEBQQDAgUEBQIFAAUCBQB1AAC5BhIT5wIAFhQAlAsRGOYCABEZAKMCeAAYABgBFgEWARQDFAMSBRIFEgUQBxAJDgcOCQ4LDgsMCwwNCg0KDQoNCA8GDwYPBhEEEQQRBBMEEQITAhUCEwAVAO0FFhPnAgAUEgD+BQ==",
  "g":"BgABArkBkAeACQCNCw8ZERkRFxEVExMVERUPFQ8XDRcLGQkZBxsFGwUdAR0BDQALAA0ADQINAAsCDQANAg0CDQILAg0EDQINBA0GDQQNBg0EDQYNCA0GDwgNCA0IDQgPCg0KDwwNDA8MDw4PDqIB7gEQDRALEAkQCQ4JEAcOBw4FDgUOAwwFDgMMAQwBDAEMAQwACgEKAAoACAIIAAgCCAIGAggCBgIGBAYCBgQEAgYEAqIBAQADAAEBAwADAAMABQADAAUAAwAFAAMABQAFAAMABQA3ABMAEwIRAhMCEQQRBBEEEQYRBg8IDwgPCA0KDQoNCg0MCwwLDgsOCQ4JDgkQBxAHEgcSBRIDFAMWAxQBFgEYABgA/gIAFgIWAhQEFgQUBBIGFAgSCBIIEAoSChAKDgwODA4MDg4MDgwODA4KEAgQCBAIEgYSBhIEEgYSBBQCEgIUAhQCOgAQABABDgEQAQ4BEAMOAw4FDgUOBQwFDgcMBQ4HDAkMB4oBUBgACbsCzQYAnAR/AC0RES0AnQMSKy4RgAEA",
  "f":"BgABAUH8A6QJBwAHAAUABwEFAQcBBQEFAwUDBQMDAwMDAwUDAwMFAQUAwQHCAQAWEgDZAhUUwQEAAOMEFhftAgAWFADKCQoSChIKEAoQCg4KDgwOCgwMDAoKDAwMCgwIDAgMCAwIDAYOCAwEDgYMBA4GDAIOBA4CDgQOAg4CDgAOAg4ADgC2AQAcDgDRAhkQowEA",
  "i":"BgACAQlQABISALoIERLqAgAREQC5CBIR6QIAAWELyAoADgIOAgwEDgIKBgwGCgYKCAoGCAgICggIBggGCgYKBAoECgQMBAoCDAIMAgwCDAAMAAwADAEMAQoBDAMKAwoDCgUKBQgFCgUIBwgHCAcICQgJBgkECwQJBA0CCwANAA0ADQELAQ0BCwMJBQsFCQUJBwkFBwcHBwcJBQcFCQUJBQkDCQMLAwkBCwELAQsACwALAAsCCwILAgkCCwIJBAkECQQJBgcGCQYHCAcIBwgHCgUKBQwFCgMMAQwBDgEMAA4=",
  "j":"BgACAWFKyAoADgIOAgwEDgIKBgwGCgYKCAoGCAgICggIBggGCgYKBAoECgQMBAoCDAIMAgwCDAAMAAwADAEMAQoBDAMKAwoDCgUKBQgFCgUIBwgHCAcICQgJBgkECwQJBA0CCwANAA0ADQELAQ0BCwMJBQsFCQUJBwkFBwcHBwcJBQcFCQUJBQkDCQMLAwkBCwELAQsACwALAAsCCwILAgkCCwIJBAkECQQJBgcGCQYHCAcIBwgHCgUKBQwFCgMMAQwBDgEMAA4BO+YCnwwJEQkRCQ8JDwsNCQ0LDQkLCwsJCQsLCQkLBwsHCwcLBwsFCwcNAwsFDQMLBQ0BDQMNAQ0DDQENAQ0ADQENAA0AVwAbDQDSAhoPQgAIAAgABgAIAgYCCAIGAgYEBgQGBAQEBAQEBgQEBAYCBgC4CRES6gIAEREAowo=",
  "k":"BgABARKoA/QFIAC0AYoD5gIAjwK5BJICwwTfAgDDAbIDFwAAnwMSEeUCABISAJILERLmAgAREQCvBQ==",
  "n":"BgABAW1yggmQAU8GBAgEBgQGBgYCCAQGBAYEBgQIAgYECAQGAggEBgIIBAgCCAQIAggCCAIIAgoACAIKAAgCCgAKAgoADAAKAgwAFgAWARQAFAEUAxQDFAMSAxIFEgUQBRIHEAkOBxAJDgsOCwwLDA0MDQoPCA8IEQgRBhEGEwYVBBUEFQIXAhkCGQDtBRQR5QIAFBAA/AUACAEIAQYBCAMGBQQFBgUEBwQFBAcCBwIHAgcCCQIHAAcACQAHAQcABwMHAQUDBwMFAwUFBQUDBQEFAwcBBwAHAPkFEhHjAgASEgDwCBAA",
  "m":"BgABAZoBfoIJigFbDAwMCg4KDggOCA4IDgYQBhAGEAQQBBAEEAISAhACEgAmASQDJAciCyANHhEcFRwXDg4QDBAKEAwQCBAKEggSBhIGEgYSBBQEEgIUAhICFAAUABQBEgEUARIDEgMSAxIFEgUQBxAHEAcQBw4JDgkOCw4LDAsMDQoNCg8KDwgPCBEIEQYRBBMEEwQTAhMCFQAVAP0FEhHlAgASEgCCBgAIAQgBBgEGAwYFBgUEBQQHBAUEBwIHAgcCBwIJAAcABwAJAAcBBwEHAQUBBwMFAwUDBQMDBQMFAwUBBQEHAQcAgQYSEeUCABISAIIGAAgBCAEGAQYDBgUGBQQFBAcEBQQHAgcCBwIHAgkABwAHAAkABwEHAQcBBQEHAwUDBQMFAwMFAwUDBQEFAQcBBwCBBhIR5QIAEhIA8AgYAA==",
  "l":"BgABAQnAAwDrAgASFgDWCxEa6gIAERkA0wsUFw==",
  "y":"BgABAZ8BogeNAg8ZERkRFxEVExMVERUPFQ8XDRcLGQkZBxsFGwUdAR0BDQALAA0ADQINAAsCDQANAg0CDQILAg0EDQINBA0GDQQNBg0EDQYNCA0GDwgNCA0IDQgPCg0KDwwNDA8MDw4PDqIB7gEQDRALEAkQCQ4JEAcOBw4FDgUOAwwFDgMMAQwBDAEMAQwACgEKAAoACAIIAAgCCAIGAggCBgIGBAYCBgQEAgYEAqIBAQADAAEBAwADAAMABQADAAUAAwAFAAMABQAFAAMABQA3ABMAEwIRABECEwQRAg8EEQQPBBEGDwgNCA8IDQgNCg0MDQwLDAkOCw4JDgcQBxAHEgUSBRQFFAMWARgDGAEaABwA9AUTEuQCABEPAP8FAAUCBQAFAgUEBQIDBAUEAwQDBgMEAQYDBgEGAAgBBgCAAQAAvAYREuICABMPAP0K",
  "q":"BgABAmj0A4YJFgAWARQAEgESAxADEAMOAw4FDgUMBQ4HDgcOBwwJDgmeAU4A2QwWGesCABYaAN4DAwADAAMBAwADAAUAAwADAAMABQAFAAUABwAHAQcACQAVABUCFQATAhUCEwQRAhMEEQQRBhEGDwgPCA8IDQoNDA0MCwwLDgkOCRAJEAkQBxIHEgUUBRYDFgMYARoBGgAcAP4CABYCFgIWBBYEFAQSBhQIEggSCBAKEgoQDA4MDgwODg4ODBAMDgwQChIIEAoSCBIGEgYUBhQEFAQWAhYCFgIWAApbkQYSKy4ReAAAjARTEjkRHykJMwDvAg==",
  "p":"BgABAmiCBIYJFgAWARYBFAEWAxQDEgUUBRIFEgcSBxAJEAkQCQ4LDgsOCwwNDA0KDwoPCg8IEQgRCBEGEwQTBhMCFQQVAhUAFQD9AgAbARkBFwMXAxcDEwUTBxMHEQcRCQ8JDQsNCw0LCw0LDQkPCQ0JDwURBxEFEQURAxMDEQMTARUBEwEVARUBFQAJAAcABwAFAAcABQAFAAMAAwADAAUAAwIDAAMAAwIDAADdAxYZ6wIAFhoA2gyeAU0OCgwIDgoMCA4GDgYMBg4GDgQQBBAEEgQUAhQCFgIWAApcoQMJNB8qNxJVEQCLBHgALhISLADwAg==",
  "o":"BgABAoMB8gOICRYAFgEWARQBFgMUAxIDFAUSBRIHEgcQBxAJEAkOCw4LDgsMDQwNCg8KDwoPCg8IEQgRBhMGEwQTBBMCFQIVABcAiwMAFwEVARUDEwMTAxMFEwcRBxEHDwkPCQ8LDQsNCw0NCw0LDwkNCw8HEQkPBxEHEQcRBRMFEwMTAxUDFQEVABUAFQAVAhUCFQITBBMEEwYTBhEGEQgRCA8KDwoPCg0KDQwNDAsOCw4JDgkQCRAJEgcSBxIFFAUUAxQDFgEWARYAFgCMAwAYAhYCFgQUBBQEFAYUCBIIEggQChAKEAwODA4MDg4MDgwQCg4KEgoQChIIEggSBhQGEgYUBBYEFAIWAhYCFgALYv0CHTZBFEMRHTcAjwMcNUITQhIiOACQAw==",
  "r":"BgACAQRigAkQAA8AAAABShAAhAFXDAwODAwKDgoOCBAIDgYQBhAEEAQQBBAEEAISABACEAAQAA4BEAAQARADEAEQAxADEAUSBRIHFAcUCxQLFA0WDVJFsQHzAQsMDQwLCgkICwgLCAkGCQYJBAkGBwIJBAcCBwQHAAcCBwAFAgcABQAHAQUABQEFAQUBBQEDAQUBAwMDAQMDAwEAmwYSEeMCABISAO4IEAA=",
  "u":"BgABAV2KBwGPAVANCQsHDQcNBw0FCwUNBQ0FDQMPAw8DEQMTARMBFQEVABUAFQITABMEEwITBBMEEQQRBhEGDwYRCA8KDQgPCg0MDQwLDAsOCRALDgcQBxIHEgUUBRQFFAMWAxgBGAEYARoA7gUTEuYCABMPAPsFAAcCBwIFBAcCBQYDBgUGAwgDBgMIAQgBCAEIAQoBCAAIAAoACAIIAggCCAIGBAgEBgQGBgYGBAYCBgQIAggACAD6BRES5AIAEREA7wgPAA==",
  "s":"BgABAasC/gLwBQoDCgMMBQ4DDgUOBRAFEAUSBRAHEgcQCRIJEAkSCxALEAsQDRANDg0ODw4PDA8MDwoRChEIEwYTBBcCFQIXABkBGQEXAxcFFQUTBRMHEwcRCREJDwkNCQ8LDQ0LCwsNCw0JDQkPBw8HDwUPBREDEQMRAREDEQETABEBEwARABMADwIRABECEQIRBBMCEwQVBBUEFQYVBhMIFwgVChUKFQxgsAIIAwYDCAMKAQgDCAMKAQoDCgEKAwoBCgMKAQwDCgEKAwoBDAMKAQoBCgEMAQoACgEKAAoBCgAKAQgACgAIAQgABgoECAIKAgoCCgAMAQoBDAUEBwIHBAcEBwIHBAkECQQJBAkECQYLBAkGCwYJBgsGCwYJCAsGCwgJBgsICQgLCAkICwgJCgkKCQoJCgcKCQwHDAcMBwwFDAcMAw4FDAMOAw4BDgMQARAAEAESABIAEgIQAg4CDgIOBA4CDgQMBAwEDAQMBgoECgYKBgoGCgYIBggGCAgIBggGBgYIBgYGBgYGBgYGBAgGBgQIBAYECAQQChIIEggSBhIEEgQSBBQCFAISABQAEgASABIAEgESARIBEAEQAxIDDgMQAxADDgUOBQwDDAMMAwoDCAMIAQYBe6cCAwIDAgUAAwIFAgUCBwIFAgcCBQIHAgUCBwIHAAUCBwIHAgUABwIHAgcABQIHAAcCBwAFAgUABQIFAAUABQIDAAEAAQABAQEAAQEBAQEBAQEBAQEDAQEAAwEBAQMAAwEDAAMBAwADAQMAAwABAQMAAwADAAEAAwIBAAMCAQQDAgE=",
  "t":"BgABAUe8BLACWAAaEADRAhsOaQANAA0ADwINAA0CDQANAg0CDQINBA0CCwYNBA0GCwYNBgsIDQgLCAsKCwgJDAsKCQwJDAkOCQ4HEAcSBxIHEgUUAOAEawAVEQDWAhYTbAAAygIVFOYCABUXAMUCogEAFhQA1QIVEqEBAADzAwIFBAMEBQQDBAMEAwYDBgMGAwYBCAEGAQgBBgEIAAgA",
  "w":"BgABARz8BsAEINYCKNgBERLuAgARD+8B3QgSEc0CABQSW7YCV7UCFBHJAgASEpMC3AgREvACABERmAHxBDDaAVeYAxES7gIAEREo1QE81wIIAA==",
  "z":"BgABAQ6cA9AGuQIAFw8AzAIaC9QFAAAr9wKjBuACABYQAMsCGQyZBgCaA9AG"
   }';
BEGIN

  IF font IS NULL THEN
    font := font_default;
  END IF;

  -- For character spacing, use m as guide size
  geom := ST_GeomFromTWKB(decode(font->>'m', 'base64'));
  m_width := ST_XMax(geom) - ST_XMin(geom);
  spacing := m_width / 12;

  letterarray := regexp_split_to_array(replace(letters, ' ', E'\t'), E'');
  FOREACH letter IN ARRAY letterarray
  LOOP
    geom := ST_GeomFromTWKB(decode(font->>(letter), 'base64'));
    -- Chars are not already zeroed out, so do it now
    geom := ST_Translate(geom, -1 * ST_XMin(geom), 0.0);
    -- unknown characters are treated as spaces
    IF geom IS NULL THEN
      -- spaces are a "quarter m" in width
      width := m_width / 3.5;
    ELSE
      width := (ST_XMax(geom) - ST_XMin(geom));
    END IF;
    geom := ST_Translate(geom, position, 0.0);
    -- Tighten up spacing when characters have a large gap
    -- between them like Yo or To
    adjustment := 0.0;
    IF prevgeom IS NOT NULL AND geom IS NOT NULL THEN
      dist = ST_Distance(prevgeom, geom);
      IF dist > spacing THEN
        adjustment = spacing - dist;
        geom := ST_Translate(geom, adjustment, 0.0);
      END IF;
    END IF;
    prevgeom := geom;
    position := position + width + spacing + adjustment;
    wordarr := array_append(wordarr, geom);
  END LOOP;
  -- apply the start point and scaling options
  wordgeom := ST_CollectionExtract(ST_Collect(wordarr));
  wordgeom := ST_Scale(wordgeom,
                text_height/font_default_height,
                text_height/font_default_height);
  return wordgeom;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 250
  SET "standard_conforming_strings"="on";

-- ----------------------------
-- Function structure for st_linecrossingdirection
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linecrossingdirection"("line1" "public"."geometry", "line2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_linecrossingdirection"("line1" "public"."geometry", "line2" "public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_LineCrossingDirection'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_lineextend
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineextend"("geom" "public"."geometry", "distance_forward" float8, "distance_backward" float8);
CREATE OR REPLACE FUNCTION "public"."st_lineextend"("geom" "public"."geometry", "distance_forward" float8, "distance_backward" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geometry_line_extend'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_linefromencodedpolyline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefromencodedpolyline"("txtin" text, "nprecision" int4);
CREATE OR REPLACE FUNCTION "public"."st_linefromencodedpolyline"("txtin" text, "nprecision" int4=5)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'line_from_encoded_polyline'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linefrommultipoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefrommultipoint"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_linefrommultipoint"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_line_from_mpoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_linefromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_linefromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_LineString'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linefromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_linefromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_LineString'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linefromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_linefromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_LineString'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_linefromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linefromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_linefromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_LineString'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_lineinterpolatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoint"("public"."geography", float8, "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoint"("public"."geography", float8, "use_spheroid" bool=true)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_line_interpolate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_lineinterpolatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoint"(text, float8);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoint"(text, float8)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_LineInterpolatePoint($1::public.geometry, $2);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_lineinterpolatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoint"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoint"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_line_interpolate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_lineinterpolatepoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoints"("public"."geography", float8, "use_spheroid" bool, "repeat" bool);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoints"("public"."geography", float8, "use_spheroid" bool=true, "repeat" bool=true)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_line_interpolate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_lineinterpolatepoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoints"(text, float8);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoints"(text, float8)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_LineInterpolatePoints($1::public.geometry, $2);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_lineinterpolatepoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_lineinterpolatepoints"("public"."geometry", float8, "repeat" bool);
CREATE OR REPLACE FUNCTION "public"."st_lineinterpolatepoints"("public"."geometry", float8, "repeat" bool=true)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_line_interpolate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linelocatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linelocatepoint"("public"."geography", "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_linelocatepoint"("public"."geography", "public"."geography", "use_spheroid" bool=true)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_line_locate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_linelocatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linelocatepoint"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_linelocatepoint"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_line_locate_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linelocatepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linelocatepoint"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_linelocatepoint"(text, text)
  RETURNS "pg_catalog"."float8" AS $BODY$ SELECT public.ST_LineLocatePoint($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_linemerge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linemerge"("public"."geometry", bool);
CREATE OR REPLACE FUNCTION "public"."st_linemerge"("public"."geometry", bool)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'linemerge'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_linemerge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linemerge"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_linemerge"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'linemerge'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_linestringfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linestringfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_linestringfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_LineString'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_linestringfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linestringfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_linestringfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_LineString'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_linesubstring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linesubstring"(text, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_linesubstring"(text, float8, float8)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_LineSubstring($1::public.geometry, $2, $3);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_linesubstring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linesubstring"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_linesubstring"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_line_substring'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_linesubstring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linesubstring"("public"."geography", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_linesubstring"("public"."geography", float8, float8)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_line_substring'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_linetocurve
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_linetocurve"("geometry" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_linetocurve"("geometry" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_line_desegmentize'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_locatealong
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_locatealong"("geometry" "public"."geometry", "measure" float8, "leftrightoffset" float8);
CREATE OR REPLACE FUNCTION "public"."st_locatealong"("geometry" "public"."geometry", "measure" float8, "leftrightoffset" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_LocateAlong'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_locatebetween
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_locatebetween"("geometry" "public"."geometry", "frommeasure" float8, "tomeasure" float8, "leftrightoffset" float8);
CREATE OR REPLACE FUNCTION "public"."st_locatebetween"("geometry" "public"."geometry", "frommeasure" float8, "tomeasure" float8, "leftrightoffset" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_LocateBetween'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_locatebetweenelevations
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_locatebetweenelevations"("geometry" "public"."geometry", "fromelevation" float8, "toelevation" float8);
CREATE OR REPLACE FUNCTION "public"."st_locatebetweenelevations"("geometry" "public"."geometry", "fromelevation" float8, "toelevation" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_LocateBetweenElevations'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_longestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_longestline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_longestline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS $BODY$SELECT public._ST_LongestLine(public.ST_ConvexHull($1), public.ST_ConvexHull($2))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_m
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_m"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_m"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_m_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_makebox2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makebox2d"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_makebox2d"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."box2d" AS '$libdir/postgis-3', 'BOX2D_construct'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_makeenvelope
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makeenvelope"(float8, float8, float8, float8, int4);
CREATE OR REPLACE FUNCTION "public"."st_makeenvelope"(float8, float8, float8, float8, int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_MakeEnvelope'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makeline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makeline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_makeline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makeline'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makeline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makeline"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_makeline"("public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makeline_garray'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepoint"(float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_makepoint"(float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepoint"(float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_makepoint"(float8, float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepoint"(float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_makepoint"(float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepointm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepointm"(float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_makepointm"(float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoint3dm'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepolygon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepolygon"("public"."geometry", "public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_makepolygon"("public"."geometry", "public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makepolygon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makepolygon"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_makepolygon"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_makevalid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makevalid"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_makevalid"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_MakeValid'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_makevalid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_makevalid"("geom" "public"."geometry", "params" text);
CREATE OR REPLACE FUNCTION "public"."st_makevalid"("geom" "public"."geometry", "params" text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_MakeValid'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_maxdistance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_maxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_maxdistance"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."float8" AS $BODY$SELECT public._ST_MaxDistance(public.ST_ConvexHull($1), public.ST_ConvexHull($2))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_maximuminscribedcircle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_maximuminscribedcircle"("public"."geometry", OUT "center" "public"."geometry", OUT "nearest" "public"."geometry", OUT "radius" float8);
CREATE OR REPLACE FUNCTION "public"."st_maximuminscribedcircle"(IN "public"."geometry", OUT "center" "public"."geometry", OUT "nearest" "public"."geometry", OUT "radius" float8)
  RETURNS "pg_catalog"."record" AS '$libdir/postgis-3', 'ST_MaximumInscribedCircle'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_memsize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_memsize"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_memsize"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_mem_size'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_minimumboundingcircle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_minimumboundingcircle"("inputgeom" "public"."geometry", "segs_per_quarter" int4);
CREATE OR REPLACE FUNCTION "public"."st_minimumboundingcircle"("inputgeom" "public"."geometry", "segs_per_quarter" int4=48)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_MinimumBoundingCircle'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_minimumboundingradius
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_minimumboundingradius"("public"."geometry", OUT "center" "public"."geometry", OUT "radius" float8);
CREATE OR REPLACE FUNCTION "public"."st_minimumboundingradius"(IN "public"."geometry", OUT "center" "public"."geometry", OUT "radius" float8)
  RETURNS "pg_catalog"."record" AS '$libdir/postgis-3', 'ST_MinimumBoundingRadius'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_minimumclearance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_minimumclearance"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_minimumclearance"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'ST_MinimumClearance'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_minimumclearanceline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_minimumclearanceline"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_minimumclearanceline"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_MinimumClearanceLine'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_mlinefromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mlinefromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_mlinefromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_MultiLineString'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mlinefromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mlinefromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_mlinefromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE
	WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_MultiLineString'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mlinefromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mlinefromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_mlinefromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiLineString'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_mlinefromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mlinefromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_mlinefromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_MultiLineString'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_mpointfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpointfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_mpointfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromText($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mpointfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpointfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_mpointfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mpointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpointfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_mpointfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_mpointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpointfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_mpointfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_mpolyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpolyfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_mpolyfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mpolyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpolyfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_mpolyfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_mpolyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpolyfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_mpolyfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_mpolyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_mpolyfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_mpolyfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- ----------------------------
-- Function structure for st_multi
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multi"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_multi"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_force_multi'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multilinefromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multilinefromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_multilinefromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiLineString'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multilinestringfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multilinestringfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_multilinestringfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_MLineFromText($1)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_multilinestringfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multilinestringfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_multilinestringfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_MLineFromText($1, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_multipointfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipointfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_multipointfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_MPointFromText($1)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_multipointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipointfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_multipointfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1,$2)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multipointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipointfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_multipointfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiPoint'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multipolyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipolyfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_multipolyfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multipolyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipolyfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_multipolyfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_MultiPolygon'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_multipolygonfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipolygonfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_multipolygonfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_MPolyFromText($1, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_multipolygonfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_multipolygonfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_multipolygonfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_MPolyFromText($1)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_ndims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ndims"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_ndims"("public"."geometry")
  RETURNS "pg_catalog"."int2" AS '$libdir/postgis-3', 'LWGEOM_ndims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_node
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_node"("g" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_node"("g" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Node'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_normalize"("geom" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_normalize"("geom" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_npoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_npoints"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_npoints"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_npoints'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_nrings
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_nrings"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_nrings"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_nrings'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_numcurves
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numcurves"("geometry" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numcurves"("geometry" "public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'ST_NumCurves'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_numgeometries
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numgeometries"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numgeometries"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_numgeometries_collection'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_numinteriorring
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numinteriorring"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numinteriorring"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_numinteriorrings_polygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_numinteriorrings
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numinteriorrings"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numinteriorrings"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_numinteriorrings_polygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_numpatches
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numpatches"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numpatches"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_numpatches'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_numpoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_numpoints"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_numpoints"("public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_numpoints_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_offsetcurve
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_offsetcurve"("line" "public"."geometry", "distance" float8, "params" text);
CREATE OR REPLACE FUNCTION "public"."st_offsetcurve"("line" "public"."geometry", "distance" float8, "params" text=''::text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_OffsetCurve'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_orderingequals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_orderingequals"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_orderingequals"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_same'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_orientedenvelope
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_orientedenvelope"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_orientedenvelope"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_OrientedEnvelope'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_overlaps
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_overlaps"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'overlaps'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_patchn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_patchn"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_patchn"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_patchn'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_perimeter
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_perimeter"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_perimeter"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_perimeter2d_poly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_perimeter
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_perimeter"("geog" "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_perimeter"("geog" "public"."geography", "use_spheroid" bool=true)
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'geography_perimeter'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_perimeter2d
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_perimeter2d"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_perimeter2d"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_perimeter2d_poly'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_point
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_point"(float8, float8, "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_point"(float8, float8, "srid" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Point'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_point
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_point"(float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_point"(float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_makepoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointfromgeohash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointfromgeohash"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_pointfromgeohash"(text, int4=NULL::integer)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'point_from_geohash'
  LANGUAGE c IMMUTABLE
  COST 50;

-- ----------------------------
-- Function structure for st_pointfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_pointfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_Point'
	THEN public.ST_GeomFromText($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_pointfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_pointfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_Point'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_pointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_pointfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_Point'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_pointfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_Point'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointinsidecircle
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointinsidecircle"("public"."geometry", float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_pointinsidecircle"("public"."geometry", float8, float8, float8)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'LWGEOM_inside_circle_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_pointm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointm"("xcoordinate" float8, "ycoordinate" float8, "mcoordinate" float8, "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_pointm"("xcoordinate" float8, "ycoordinate" float8, "mcoordinate" float8, "srid" int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_PointM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointn
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointn"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_pointn"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_pointn_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointonsurface
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointonsurface"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_pointonsurface"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pointonsurface'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_points
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_points"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_points"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Points'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_pointz
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointz"("xcoordinate" float8, "ycoordinate" float8, "zcoordinate" float8, "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_pointz"("xcoordinate" float8, "ycoordinate" float8, "zcoordinate" float8, "srid" int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_PointZ'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_pointzm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_pointzm"("xcoordinate" float8, "ycoordinate" float8, "zcoordinate" float8, "mcoordinate" float8, "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_pointzm"("xcoordinate" float8, "ycoordinate" float8, "zcoordinate" float8, "mcoordinate" float8, "srid" int4=0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_PointZM'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polyfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_polyfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1, $2)) = 'ST_Polygon'
	THEN public.ST_GeomFromText($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_polyfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polyfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_polyfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromText($1)) = 'ST_Polygon'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_polyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polyfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_polyfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_Polygon'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polyfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polyfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_polyfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1, $2)) = 'ST_Polygon'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polygon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygon"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_polygon"("public"."geometry", int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT public.ST_SetSRID(public.ST_MakePolygon($1), $2)
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polygonfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygonfromtext"(text, int4);
CREATE OR REPLACE FUNCTION "public"."st_polygonfromtext"(text, int4)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_PolyFromText($1, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_polygonfromtext
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygonfromtext"(text);
CREATE OR REPLACE FUNCTION "public"."st_polygonfromtext"(text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_PolyFromText($1)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_polygonfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygonfromwkb"(bytea);
CREATE OR REPLACE FUNCTION "public"."st_polygonfromwkb"(bytea)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1)) = 'ST_Polygon'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polygonfromwkb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygonfromwkb"(bytea, int4);
CREATE OR REPLACE FUNCTION "public"."st_polygonfromwkb"(bytea, int4)
  RETURNS "public"."geometry" AS $BODY$
	SELECT CASE WHEN public.ST_GeometryType(public.ST_GeomFromWKB($1,$2)) = 'ST_Polygon'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_polygonize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_polygonize"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_polygonize"("public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'polygonize_garray'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_project
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_project"("geog" "public"."geography", "distance" float8, "azimuth" float8);
CREATE OR REPLACE FUNCTION "public"."st_project"("geog" "public"."geography", "distance" float8, "azimuth" float8)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_project'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_project
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_project"("geog_from" "public"."geography", "geog_to" "public"."geography", "distance" float8);
CREATE OR REPLACE FUNCTION "public"."st_project"("geog_from" "public"."geography", "geog_to" "public"."geography", "distance" float8)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_project_geography'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_project
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_project"("geom1" "public"."geometry", "geom2" "public"."geometry", "distance" float8);
CREATE OR REPLACE FUNCTION "public"."st_project"("geom1" "public"."geometry", "geom2" "public"."geometry", "distance" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geometry_project_geometry'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_project
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_project"("geom1" "public"."geometry", "distance" float8, "azimuth" float8);
CREATE OR REPLACE FUNCTION "public"."st_project"("geom1" "public"."geometry", "distance" float8, "azimuth" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'geometry_project_direction'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_quantizecoordinates
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_quantizecoordinates"("g" "public"."geometry", "prec_x" int4, "prec_y" int4, "prec_z" int4, "prec_m" int4);
CREATE OR REPLACE FUNCTION "public"."st_quantizecoordinates"("g" "public"."geometry", "prec_x" int4, "prec_y" int4=NULL::integer, "prec_z" int4=NULL::integer, "prec_m" int4=NULL::integer)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_QuantizeCoordinates'
  LANGUAGE c IMMUTABLE
  COST 250;

-- ----------------------------
-- Function structure for st_reduceprecision
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_reduceprecision"("geom" "public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_reduceprecision"("geom" "public"."geometry", "gridsize" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_ReducePrecision'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_relate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry", text);
CREATE OR REPLACE FUNCTION "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry", text)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'relate_pattern'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_relate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'relate_full'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_relate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_relate"("geom1" "public"."geometry", "geom2" "public"."geometry", int4)
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'relate_full'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_relatematch
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_relatematch"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_relatematch"(text, text)
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'ST_RelateMatch'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_removeirrelevantpointsforview
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_removeirrelevantpointsforview"("public"."geometry", "public"."box2d", bool);
CREATE OR REPLACE FUNCTION "public"."st_removeirrelevantpointsforview"("public"."geometry", "public"."box2d", bool=false)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_RemoveIrrelevantPointsForView'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_removepoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_removepoint"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_removepoint"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_removepoint'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_removerepeatedpoints
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_removerepeatedpoints"("geom" "public"."geometry", "tolerance" float8);
CREATE OR REPLACE FUNCTION "public"."st_removerepeatedpoints"("geom" "public"."geometry", "tolerance" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_RemoveRepeatedPoints'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_removesmallparts
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_removesmallparts"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_removesmallparts"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_RemoveSmallParts'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_reverse
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_reverse"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_reverse"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_reverse'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotate"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_rotate"("public"."geometry", float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2), cos($2), 0,  0, 0, 1,  0, 0, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotate"("public"."geometry", float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_rotate"("public"."geometry", float8, float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2),  cos($2), 0, 0, 0, 1,	$3 - cos($2) * $3 + sin($2) * $4, $4 - sin($2) * $3 - cos($2) * $4, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotate"("public"."geometry", float8, "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_rotate"("public"."geometry", float8, "public"."geometry")
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2),  cos($2), 0, 0, 0, 1, public.ST_X($3) - cos($2) * public.ST_X($3) + sin($2) * public.ST_Y($3), public.ST_Y($3) - sin($2) * public.ST_X($3) - cos($2) * public.ST_Y($3), 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotatex
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotatex"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_rotatex"("public"."geometry", float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1, 1, 0, 0, 0, cos($2), -sin($2), 0, sin($2), cos($2), 0, 0, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotatey
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotatey"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_rotatey"("public"."geometry", float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  cos($2), 0, sin($2),  0, 1, 0,  -sin($2), 0, cos($2), 0,  0, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_rotatez
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_rotatez"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_rotatez"("public"."geometry", float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Rotate($1, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_scale
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_scale"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_scale"("public"."geometry", "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Scale'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_scale
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_scale"("public"."geometry", "public"."geometry", "origin" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_scale"("public"."geometry", "public"."geometry", "origin" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Scale'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_scale
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_scale"("public"."geometry", float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_scale"("public"."geometry", float8, float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Scale($1, public.ST_MakePoint($2, $3, $4))$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_scale
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_scale"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_scale"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Scale($1, $2, $3, 1)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_scroll
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_scroll"("public"."geometry", "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_scroll"("public"."geometry", "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Scroll'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_segmentize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_segmentize"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_segmentize"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_segmentize2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_segmentize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_segmentize"("geog" "public"."geography", "max_segment_length" float8);
CREATE OR REPLACE FUNCTION "public"."st_segmentize"("geog" "public"."geography", "max_segment_length" float8)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_segmentize'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_seteffectivearea
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_seteffectivearea"("public"."geometry", float8, int4);
CREATE OR REPLACE FUNCTION "public"."st_seteffectivearea"("public"."geometry", float8='-1'::integer, int4=1)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_SetEffectiveArea'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_setpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_setpoint"("public"."geometry", int4, "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_setpoint"("public"."geometry", int4, "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_setpoint_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_setsrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_setsrid"("geog" "public"."geography", "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_setsrid"("geog" "public"."geography", "srid" int4)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'LWGEOM_set_srid'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_setsrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_setsrid"("geom" "public"."geometry", "srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_setsrid"("geom" "public"."geometry", "srid" int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_set_srid'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_sharedpaths
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_sharedpaths"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_sharedpaths"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_SharedPaths'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_shiftlongitude
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_shiftlongitude"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_shiftlongitude"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_longitude_shift'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_shortestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_shortestline"(text, text);
CREATE OR REPLACE FUNCTION "public"."st_shortestline"(text, text)
  RETURNS "public"."geometry" AS $BODY$ SELECT public.ST_ShortestLine($1::public.geometry, $2::public.geometry);  $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_shortestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_shortestline"("public"."geography", "public"."geography", "use_spheroid" bool);
CREATE OR REPLACE FUNCTION "public"."st_shortestline"("public"."geography", "public"."geography", "use_spheroid" bool=true)
  RETURNS "public"."geography" AS '$libdir/postgis-3', 'geography_shortestline'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_shortestline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_shortestline"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_shortestline"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_shortestline2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_simplify
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_simplify"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_simplify"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_simplify2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_simplify
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_simplify"("public"."geometry", float8, bool);
CREATE OR REPLACE FUNCTION "public"."st_simplify"("public"."geometry", float8, bool)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_simplify2d'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_simplifypolygonhull
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_simplifypolygonhull"("geom" "public"."geometry", "vertex_fraction" float8, "is_outer" bool);
CREATE OR REPLACE FUNCTION "public"."st_simplifypolygonhull"("geom" "public"."geometry", "vertex_fraction" float8, "is_outer" bool=true)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_SimplifyPolygonHull'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_simplifypreservetopology
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_simplifypreservetopology"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_simplifypreservetopology"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'topologypreservesimplify'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_simplifyvw
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_simplifyvw"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_simplifyvw"("public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_SetEffectiveArea'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_snap
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_snap"("geom1" "public"."geometry", "geom2" "public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_snap"("geom1" "public"."geometry", "geom2" "public"."geometry", float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Snap'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_snaptogrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_snaptogrid"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_snaptogrid"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_SnapToGrid($1, 0, 0, $2, $3)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_snaptogrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_snaptogrid"("public"."geometry", float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_snaptogrid"("public"."geometry", float8, float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_snaptogrid'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_snaptogrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_snaptogrid"("public"."geometry", float8);
CREATE OR REPLACE FUNCTION "public"."st_snaptogrid"("public"."geometry", float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_SnapToGrid($1, 0, 0, $2, $2)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_snaptogrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_snaptogrid"("geom1" "public"."geometry", "geom2" "public"."geometry", float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_snaptogrid"("geom1" "public"."geometry", "geom2" "public"."geometry", float8, float8, float8, float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_snaptogrid_pointoff'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_split
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_split"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_split"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Split'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_square
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_square"("size" float8, "cell_i" int4, "cell_j" int4, "origin" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_square"("size" float8, "cell_i" int4, "cell_j" int4, "origin" "public"."geometry"='010100000000000000000000000000000000000000'::geometry)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Square'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_squaregrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_squaregrid"("size" float8, "bounds" "public"."geometry", OUT "geom" "public"."geometry", OUT "i" int4, OUT "j" int4);
CREATE OR REPLACE FUNCTION "public"."st_squaregrid"(IN "size" float8, IN "bounds" "public"."geometry", OUT "geom" "public"."geometry", OUT "i" int4, OUT "j" int4)
  RETURNS SETOF "pg_catalog"."record" AS '$libdir/postgis-3', 'ST_ShapeGrid'
  LANGUAGE c IMMUTABLE STRICT
  COST 250
  ROWS 1000;

-- ----------------------------
-- Function structure for st_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_srid"("geog" "public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_srid"("geog" "public"."geography")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_get_srid'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_srid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_srid"("geom" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_srid"("geom" "public"."geometry")
  RETURNS "pg_catalog"."int4" AS '$libdir/postgis-3', 'LWGEOM_get_srid'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_startpoint
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_startpoint"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_startpoint"("public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_startpoint_linestring'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_subdivide
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_subdivide"("geom" "public"."geometry", "maxvertices" int4, "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_subdivide"("geom" "public"."geometry", "maxvertices" int4=256, "gridsize" float8='-1.0'::numeric)
  RETURNS SETOF "public"."geometry" AS '$libdir/postgis-3', 'ST_Subdivide'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000
  ROWS 1000;

-- ----------------------------
-- Function structure for st_summary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_summary"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_summary"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_summary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_summary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_summary"("public"."geography");
CREATE OR REPLACE FUNCTION "public"."st_summary"("public"."geography")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_summary'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_swapordinates
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_swapordinates"("geom" "public"."geometry", "ords" cstring);
CREATE OR REPLACE FUNCTION "public"."st_swapordinates"("geom" "public"."geometry", "ords" cstring)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_SwapOrdinates'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_symdifference
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_symdifference"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_symdifference"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8='-1.0'::numeric)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_SymDifference'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_symmetricdifference
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_symmetricdifference"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_symmetricdifference"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_SymDifference(geom1, geom2, -1.0);$BODY$
  LANGUAGE sql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for st_tileenvelope
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_tileenvelope"("zoom" int4, "x" int4, "y" int4, "bounds" "public"."geometry", "margin" float8);
CREATE OR REPLACE FUNCTION "public"."st_tileenvelope"("zoom" int4, "x" int4, "y" int4, "bounds" "public"."geometry"='0102000020110F00000200000093107C45F81B73C193107C45F81B73C193107C45F81B734193107C45F81B7341'::geometry, "margin" float8=0.0)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_TileEnvelope'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_touches
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_touches"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_touches"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'touches'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_transform
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transform"("geom" "public"."geometry", "to_proj" text);
CREATE OR REPLACE FUNCTION "public"."st_transform"("geom" "public"."geometry", "to_proj" text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.postgis_transform_geometry($1, proj4text, $2, 0)
	FROM public.spatial_ref_sys WHERE srid=public.ST_SRID($1);$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_transform
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transform"("public"."geometry", int4);
CREATE OR REPLACE FUNCTION "public"."st_transform"("public"."geometry", int4)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'transform'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_transform
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transform"("geom" "public"."geometry", "from_proj" text, "to_proj" text);
CREATE OR REPLACE FUNCTION "public"."st_transform"("geom" "public"."geometry", "from_proj" text, "to_proj" text)
  RETURNS "public"."geometry" AS $BODY$SELECT public.postgis_transform_geometry($1, $2, $3, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_transform
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transform"("geom" "public"."geometry", "from_proj" text, "to_srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_transform"("geom" "public"."geometry", "from_proj" text, "to_srid" int4)
  RETURNS "public"."geometry" AS $BODY$SELECT public.postgis_transform_geometry($1, $2, proj4text, $3)
	FROM public.spatial_ref_sys WHERE srid=$3;$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_transformpipeline
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transformpipeline"("geom" "public"."geometry", "pipeline" text, "to_srid" int4);
CREATE OR REPLACE FUNCTION "public"."st_transformpipeline"("geom" "public"."geometry", "pipeline" text, "to_srid" int4=0)
  RETURNS "public"."geometry" AS $BODY$SELECT public.postgis_transform_pipeline_geometry($1, $2, TRUE, $3)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_translate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_translate"("public"."geometry", float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_translate"("public"."geometry", float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Translate($1, $2, $3, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_translate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_translate"("public"."geometry", float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_translate"("public"."geometry", float8, float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1, 1, 0, 0, 0, 1, 0, 0, 0, 1, $2, $3, $4)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_transscale
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_transscale"("public"."geometry", float8, float8, float8, float8);
CREATE OR REPLACE FUNCTION "public"."st_transscale"("public"."geometry", float8, float8, float8, float8)
  RETURNS "public"."geometry" AS $BODY$SELECT public.ST_Affine($1,  $4, 0, 0,  0, $5, 0,
		0, 0, 1,  $2 * $4, $3 * $5, 0)$BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_triangulatepolygon
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_triangulatepolygon"("g1" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_triangulatepolygon"("g1" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_TriangulatePolygon'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_unaryunion
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_unaryunion"("public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_unaryunion"("public"."geometry", "gridsize" float8='-1.0'::numeric)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_UnaryUnion'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_union
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_union"("public"."_geometry");
CREATE OR REPLACE FUNCTION "public"."st_union"("public"."_geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'pgis_union_geometry_array'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_union
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_union"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_union"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Union'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_union
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_union"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8);
CREATE OR REPLACE FUNCTION "public"."st_union"("geom1" "public"."geometry", "geom2" "public"."geometry", "gridsize" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_Union'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_voronoilines
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_voronoilines"("g1" "public"."geometry", "tolerance" float8, "extend_to" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_voronoilines"("g1" "public"."geometry", "tolerance" float8=0.0, "extend_to" "public"."geometry"=NULL::geometry)
  RETURNS "public"."geometry" AS $BODY$ SELECT public._ST_Voronoi(g1, extend_to, tolerance, false) $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_voronoipolygons
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_voronoipolygons"("g1" "public"."geometry", "tolerance" float8, "extend_to" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_voronoipolygons"("g1" "public"."geometry", "tolerance" float8=0.0, "extend_to" "public"."geometry"=NULL::geometry)
  RETURNS "public"."geometry" AS $BODY$ SELECT public._ST_Voronoi(g1, extend_to, tolerance, true) $BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;

-- ----------------------------
-- Function structure for st_within
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_within"("geom1" "public"."geometry", "geom2" "public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_within"("geom1" "public"."geometry", "geom2" "public"."geometry")
  RETURNS "pg_catalog"."bool" AS '$libdir/postgis-3', 'within'
  LANGUAGE c IMMUTABLE STRICT
  COST 5000;

-- ----------------------------
-- Function structure for st_wkbtosql
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_wkbtosql"("wkb" bytea);
CREATE OR REPLACE FUNCTION "public"."st_wkbtosql"("wkb" bytea)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_WKB'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_wkttosql
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_wkttosql"(text);
CREATE OR REPLACE FUNCTION "public"."st_wkttosql"(text)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'LWGEOM_from_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 250;

-- ----------------------------
-- Function structure for st_wrapx
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_wrapx"("geom" "public"."geometry", "wrap" float8, "move" float8);
CREATE OR REPLACE FUNCTION "public"."st_wrapx"("geom" "public"."geometry", "wrap" float8, "move" float8)
  RETURNS "public"."geometry" AS '$libdir/postgis-3', 'ST_WrapX'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for st_x
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_x"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_x"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_x_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_xmax
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_xmax"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_xmax"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_xmax'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_xmin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_xmin"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_xmin"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_xmin'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_y
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_y"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_y"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_y_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_ymax
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ymax"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_ymax"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_ymax'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_ymin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_ymin"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_ymin"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_ymin'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_z
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_z"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_z"("public"."geometry")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'LWGEOM_z_point'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_zmax
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_zmax"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_zmax"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_zmax'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_zmflag
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_zmflag"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."st_zmflag"("public"."geometry")
  RETURNS "pg_catalog"."int2" AS '$libdir/postgis-3', 'LWGEOM_zmflag'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for st_zmin
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."st_zmin"("public"."box3d");
CREATE OR REPLACE FUNCTION "public"."st_zmin"("public"."box3d")
  RETURNS "pg_catalog"."float8" AS '$libdir/postgis-3', 'BOX3D_zmin'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."halfvec", int4, int4);
CREATE OR REPLACE FUNCTION "public"."subvector"("public"."halfvec", int4, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."vector", int4, int4);
CREATE OR REPLACE FUNCTION "public"."subvector"("public"."vector", int4, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for text
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."text"("public"."geometry");
CREATE OR REPLACE FUNCTION "public"."text"("public"."geometry")
  RETURNS "pg_catalog"."text" AS '$libdir/postgis-3', 'LWGEOM_to_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 50;

-- ----------------------------
-- Function structure for update_device_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_device_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_device_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_farming_calendar_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_farming_calendar_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_farming_calendar_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
    $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_iotda_device_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_iotda_device_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_iotda_device_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_orchard_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_orchard_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_orchard_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_sys_user_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_sys_user_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_sys_user_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for update_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_timestamp"();
CREATE OR REPLACE FUNCTION "public"."update_timestamp"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for updategeometrysrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."updategeometrysrid"(varchar, varchar, int4);
CREATE OR REPLACE FUNCTION "public"."updategeometrysrid"(varchar, varchar, int4)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret  text;
BEGIN
	SELECT public.UpdateGeometrySRID('','',$1,$2,$3) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for updategeometrysrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."updategeometrysrid"("catalogn_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid_in" int4);
CREATE OR REPLACE FUNCTION "public"."updategeometrysrid"("catalogn_name" varchar, "schema_name" varchar, "table_name" varchar, "column_name" varchar, "new_srid_in" int4)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	myrec RECORD;
	okay boolean;
	cname varchar;
	real_schema name;
	unknown_srid integer;
	new_srid integer := new_srid_in;

BEGIN

	-- Find, check or fix schema_name
	IF ( schema_name != '' ) THEN
		okay = false;

		FOR myrec IN SELECT nspname FROM pg_namespace WHERE text(nspname) = schema_name LOOP
			okay := true;
		END LOOP;

		IF ( okay <> true ) THEN
			RAISE EXCEPTION 'Invalid schema name';
		ELSE
			real_schema = schema_name;
		END IF;
	ELSE
		SELECT INTO real_schema current_schema()::text;
	END IF;

	-- Ensure that column_name is in geometry_columns
	okay = false;
	FOR myrec IN SELECT type, coord_dimension FROM public.geometry_columns WHERE f_table_schema = text(real_schema) and f_table_name = table_name and f_geometry_column = column_name LOOP
		okay := true;
	END LOOP;
	IF (NOT okay) THEN
		RAISE EXCEPTION 'column not found in geometry_columns table';
		RETURN false;
	END IF;

	-- Ensure that new_srid is valid
	IF ( new_srid > 0 ) THEN
		IF ( SELECT count(*) = 0 from public.spatial_ref_sys where srid = new_srid ) THEN
			RAISE EXCEPTION 'invalid SRID: % not found in spatial_ref_sys', new_srid;
			RETURN false;
		END IF;
	ELSE
		unknown_srid := public.ST_SRID('POINT EMPTY'::public.geometry);
		IF ( new_srid != unknown_srid ) THEN
			new_srid := unknown_srid;
			RAISE NOTICE 'SRID value % converted to the officially unknown SRID value %', new_srid_in, new_srid;
		END IF;
	END IF;

	IF public.postgis_constraint_srid(real_schema, table_name, column_name) IS NOT NULL THEN
	-- srid was enforced with constraints before, keep it that way.
		-- Make up constraint name
		cname = 'enforce_srid_'  || column_name;

		-- Drop enforce_srid constraint
		EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) ||
			'.' || quote_ident(table_name) ||
			' DROP constraint ' || quote_ident(cname);

		-- Update geometries SRID
		EXECUTE 'UPDATE ' || quote_ident(real_schema) ||
			'.' || quote_ident(table_name) ||
			' SET ' || quote_ident(column_name) ||
			' = public.ST_SetSRID(' || quote_ident(column_name) ||
			', ' || new_srid::text || ')';

		-- Reset enforce_srid constraint
		EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) ||
			'.' || quote_ident(table_name) ||
			' ADD constraint ' || quote_ident(cname) ||
			' CHECK (st_srid(' || quote_ident(column_name) ||
			') = ' || new_srid::text || ')';
	ELSE
		-- We will use typmod to enforce if no srid constraints
		-- We are using postgis_type_name to lookup the new name
		-- (in case Paul changes his mind and flips geometry_columns to return old upper case name)
		EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) || '.' || quote_ident(table_name) ||
		' ALTER COLUMN ' || quote_ident(column_name) || ' TYPE  geometry(' || public.postgis_type_name(myrec.type, myrec.coord_dimension, true) || ', ' || new_srid::text || ') USING public.ST_SetSRID(' || quote_ident(column_name) || ',' || new_srid::text || ');' ;
	END IF;

	RETURN real_schema || '.' || table_name || '.' || column_name ||' SRID changed to ' || new_srid::text;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for updategeometrysrid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."updategeometrysrid"(varchar, varchar, varchar, int4);
CREATE OR REPLACE FUNCTION "public"."updategeometrysrid"(varchar, varchar, varchar, int4)
  RETURNS "pg_catalog"."text" AS $BODY$
DECLARE
	ret  text;
BEGIN
	SELECT public.UpdateGeometrySRID('',$1,$2,$3,$4) into ret;
	RETURN ret;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;

-- ----------------------------
-- Function structure for vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector"("public"."vector", int4, bool);
CREATE OR REPLACE FUNCTION "public"."vector"("public"."vector", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_accum"(_float8, "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_accum"(_float8, "public"."vector")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_add"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_add"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_avg"(_float8);
CREATE OR REPLACE FUNCTION "public"."vector_avg"(_float8)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_cmp"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_cmp"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_combine"(_float8, _float8);
CREATE OR REPLACE FUNCTION "public"."vector_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_concat"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_concat"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_dims"("public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."halfvec");
CREATE OR REPLACE FUNCTION "public"."vector_dims"("public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_eq"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_eq"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ge"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_ge"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_gt"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_gt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_in"(cstring, oid, int4);
CREATE OR REPLACE FUNCTION "public"."vector_in"(cstring, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_l2_squared_distance"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_l2_squared_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_le"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_le"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_lt"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_lt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_mul"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_mul"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ne"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_ne"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_negative_inner_product"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_negative_inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_norm"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_norm"("public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_out"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_out"("public"."vector")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'vector_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_recv"(internal, oid, int4);
CREATE OR REPLACE FUNCTION "public"."vector_recv"(internal, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_send"("public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_send"("public"."vector")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'vector_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_spherical_distance"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_spherical_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_sub"("public"."vector", "public"."vector");
CREATE OR REPLACE FUNCTION "public"."vector_sub"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_float4"("public"."vector", int4, bool);
CREATE OR REPLACE FUNCTION "public"."vector_to_float4"("public"."vector", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'vector_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_halfvec"("public"."vector", int4, bool);
CREATE OR REPLACE FUNCTION "public"."vector_to_halfvec"("public"."vector", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'vector_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_sparsevec"("public"."vector", int4, bool);
CREATE OR REPLACE FUNCTION "public"."vector_to_sparsevec"("public"."vector", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'vector_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_typmod_in"(_cstring);
CREATE OR REPLACE FUNCTION "public"."vector_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- View structure for geography_columns
-- ----------------------------
DROP VIEW IF EXISTS "public"."geography_columns";
CREATE VIEW "public"."geography_columns" AS  SELECT current_database() AS f_table_catalog,
    n.nspname AS f_table_schema,
    c.relname AS f_table_name,
    a.attname AS f_geography_column,
    postgis_typmod_dims(a.atttypmod) AS coord_dimension,
    postgis_typmod_srid(a.atttypmod) AS srid,
    postgis_typmod_type(a.atttypmod) AS type
   FROM pg_class c,
    pg_attribute a,
    pg_type t,
    pg_namespace n
  WHERE t.typname = 'geography'::name AND a.attisdropped = false AND a.atttypid = t.oid AND a.attrelid = c.oid AND c.relnamespace = n.oid AND (c.relkind = ANY (ARRAY['r'::"char", 'v'::"char", 'm'::"char", 'f'::"char", 'p'::"char"])) AND NOT pg_is_other_temp_schema(c.relnamespace) AND has_table_privilege(c.oid, 'SELECT'::text);

-- ----------------------------
-- View structure for geometry_columns
-- ----------------------------
DROP VIEW IF EXISTS "public"."geometry_columns";
CREATE VIEW "public"."geometry_columns" AS  SELECT current_database()::character varying(256) AS f_table_catalog,
    n.nspname AS f_table_schema,
    c.relname AS f_table_name,
    a.attname AS f_geometry_column,
    COALESCE(postgis_typmod_dims(a.atttypmod), sn.ndims, 2) AS coord_dimension,
    COALESCE(NULLIF(postgis_typmod_srid(a.atttypmod), 0), sr.srid, 0) AS srid,
    replace(replace(COALESCE(NULLIF(upper(postgis_typmod_type(a.atttypmod)), 'GEOMETRY'::text), st.type, 'GEOMETRY'::text), 'ZM'::text, ''::text), 'Z'::text, ''::text)::character varying(30) AS type
   FROM pg_class c
     JOIN pg_attribute a ON a.attrelid = c.oid AND NOT a.attisdropped
     JOIN pg_namespace n ON c.relnamespace = n.oid
     JOIN pg_type t ON a.atttypid = t.oid
     LEFT JOIN ( SELECT s.connamespace,
            s.conrelid,
            s.conkey,
            (regexp_match(s.consrc, 'geometrytype\(\w+\)\s*=\s*''(\w+)'''::text, 'i'::text))[1] AS type
           FROM ( SELECT pg_constraint.connamespace,
                    pg_constraint.conrelid,
                    pg_constraint.conkey,
                    pg_get_constraintdef(pg_constraint.oid) AS consrc
                   FROM pg_constraint) s
          WHERE s.consrc ~* 'geometrytype\(\w+\)\s*=\s*''\w+'''::text) st ON st.connamespace = n.oid AND st.conrelid = c.oid AND (a.attnum = ANY (st.conkey))
     LEFT JOIN ( SELECT s.connamespace,
            s.conrelid,
            s.conkey,
            (regexp_match(s.consrc, 'ndims\(\w+\)\s*=\s*(\d+)'::text, 'i'::text))[1]::integer AS ndims
           FROM ( SELECT pg_constraint.connamespace,
                    pg_constraint.conrelid,
                    pg_constraint.conkey,
                    pg_get_constraintdef(pg_constraint.oid) AS consrc
                   FROM pg_constraint) s
          WHERE s.consrc ~* 'ndims\(\w+\)\s*=\s*\d+'::text) sn ON sn.connamespace = n.oid AND sn.conrelid = c.oid AND (a.attnum = ANY (sn.conkey))
     LEFT JOIN ( SELECT s.connamespace,
            s.conrelid,
            s.conkey,
            (regexp_match(s.consrc, 'srid\(\w+\)\s*=\s*(\d+)'::text, 'i'::text))[1]::integer AS srid
           FROM ( SELECT pg_constraint.connamespace,
                    pg_constraint.conrelid,
                    pg_constraint.conkey,
                    pg_get_constraintdef(pg_constraint.oid) AS consrc
                   FROM pg_constraint) s
          WHERE s.consrc ~* 'srid\(\w+\)\s*=\s*\d+'::text) sr ON sr.connamespace = n.oid AND sr.conrelid = c.oid AND (a.attnum = ANY (sr.conkey))
  WHERE (c.relkind = ANY (ARRAY['r'::"char", 'v'::"char", 'm'::"char", 'f'::"char", 'p'::"char"])) AND NOT c.relname = 'raster_columns'::name AND t.typname = 'geometry'::name AND NOT pg_is_other_temp_schema(c.relnamespace) AND has_table_privilege(c.oid, 'SELECT'::text);
CREATE RULE "geometry_columns_insert" AS ON INSERT TO "public"."geometry_columns" DO INSTEAD NOTHING;;
CREATE RULE "geometry_columns_update" AS ON UPDATE TO "public"."geometry_columns" DO INSTEAD NOTHING;;
CREATE RULE "geometry_columns_delete" AS ON DELETE TO "public"."geometry_columns" DO INSTEAD NOTHING;;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."alert_message_id_seq"
OWNED BY "public"."alert_message"."id";
SELECT setval('"public"."alert_message_id_seq"', 1060, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."alert_rule_id_seq"
OWNED BY "public"."alert_rule"."id";
SELECT setval('"public"."alert_rule_id_seq"', 3, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."device_id_seq"
OWNED BY "public"."device"."id";
SELECT setval('"public"."device_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."disease_catalog_id_seq"
OWNED BY "public"."disease_catalog"."id";
SELECT setval('"public"."disease_catalog_id_seq"', 14, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."disease_detection_id_seq"
OWNED BY "public"."disease_detection"."id";
SELECT setval('"public"."disease_detection_id_seq"', 54, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."environment_data_id_seq"
OWNED BY "public"."environment_data"."id";
SELECT setval('"public"."environment_data_id_seq"', 7880, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."farming_calendar_id_seq"
OWNED BY "public"."farming_calendar"."id";
SELECT setval('"public"."farming_calendar_id_seq"', 7, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."firmware_package_id_seq"
OWNED BY "public"."firmware_package"."id";
SELECT setval('"public"."firmware_package_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."iotda_device_id_seq"
OWNED BY "public"."iotda_device"."id";
SELECT setval('"public"."iotda_device_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."knowledge_chunk_id_seq"
OWNED BY "public"."knowledge_chunk"."id";
SELECT setval('"public"."knowledge_chunk_id_seq"', 78, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."knowledge_document_id_seq"
OWNED BY "public"."knowledge_document"."id";
SELECT setval('"public"."knowledge_document_id_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."orchard_id_seq"
OWNED BY "public"."orchard"."id";
SELECT setval('"public"."orchard_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."rag_feedback_id_seq"
OWNED BY "public"."rag_feedback"."id";
SELECT setval('"public"."rag_feedback_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."risk_prediction_id_seq"
OWNED BY "public"."risk_prediction"."id";
SELECT setval('"public"."risk_prediction_id_seq"', 309, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_operation_log_id_seq"
OWNED BY "public"."sys_operation_log"."id";
SELECT setval('"public"."sys_operation_log_id_seq"', 3833, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_refresh_token_id_seq"
OWNED BY "public"."sys_refresh_token"."id";
SELECT setval('"public"."sys_refresh_token_id_seq"', 3790, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_role_id_seq"
OWNED BY "public"."sys_role"."id";
SELECT setval('"public"."sys_role_id_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_id_seq"
OWNED BY "public"."sys_user"."id";
SELECT setval('"public"."sys_user_id_seq"', 100, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."user_feedback_id_seq"
OWNED BY "public"."user_feedback"."id";
SELECT setval('"public"."user_feedback_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."weather_forecast_id_seq"
OWNED BY "public"."weather_forecast"."id";
SELECT setval('"public"."weather_forecast_id_seq"', 461, true);

-- ----------------------------
-- Indexes structure for table alert_message
-- ----------------------------
CREATE INDEX "idx_alert_level" ON "public"."alert_message" USING btree (
  "level" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alert_orchard" ON "public"."alert_message" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alert_status" ON "public"."alert_message" USING btree (
  "push_status" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alert_user" ON "public"."alert_message" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alert_user_read" ON "public"."alert_message" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "read_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table alert_message
-- ----------------------------
ALTER TABLE "public"."alert_message" ADD CONSTRAINT "alert_message_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Triggers structure for table alert_rule
-- ----------------------------
CREATE TRIGGER "trg_alert_rule_update" BEFORE UPDATE ON "public"."alert_rule"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Uniques structure for table alert_rule
-- ----------------------------
ALTER TABLE "public"."alert_rule" ADD CONSTRAINT "alert_rule_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table alert_rule
-- ----------------------------
ALTER TABLE "public"."alert_rule" ADD CONSTRAINT "alert_rule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table device
-- ----------------------------
CREATE INDEX "idx_device_apikey" ON "public"."device" USING btree (
  "api_key" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_device_code" ON "public"."device" USING btree (
  "device_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_device_orchard" ON "public"."device" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table device
-- ----------------------------
CREATE TRIGGER "trg_device_update" BEFORE UPDATE ON "public"."device"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Uniques structure for table device
-- ----------------------------
ALTER TABLE "public"."device" ADD CONSTRAINT "device_device_code_key" UNIQUE ("device_code");
ALTER TABLE "public"."device" ADD CONSTRAINT "device_api_key_key" UNIQUE ("api_key");

-- ----------------------------
-- Primary Key structure for table device
-- ----------------------------
ALTER TABLE "public"."device" ADD CONSTRAINT "device_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table disease_catalog
-- ----------------------------
CREATE INDEX "idx_dc_code" ON "public"."disease_catalog" USING btree (
  "disease_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_dc_type" ON "public"."disease_catalog" USING btree (
  "disease_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table disease_catalog
-- ----------------------------
ALTER TABLE "public"."disease_catalog" ADD CONSTRAINT "disease_catalog_disease_code_key" UNIQUE ("disease_code");
ALTER TABLE "public"."disease_catalog" ADD CONSTRAINT "disease_catalog_disease_name_key" UNIQUE ("disease_name");

-- ----------------------------
-- Primary Key structure for table disease_catalog
-- ----------------------------
ALTER TABLE "public"."disease_catalog" ADD CONSTRAINT "disease_catalog_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table disease_detection
-- ----------------------------
CREATE INDEX "idx_detection_catalog" ON "public"."disease_detection" USING btree (
  "disease_catalog_id" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_detection_class" ON "public"."disease_detection" USING btree (
  "top_class_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_detection_device" ON "public"."disease_detection" USING btree (
  "device_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_detection_orchard" ON "public"."disease_detection" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_detection_orchard_time" ON "public"."disease_detection" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "device_timestamp" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_detection_time" ON "public"."disease_detection" USING btree (
  "device_timestamp" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table disease_detection
-- ----------------------------
ALTER TABLE "public"."disease_detection" ADD CONSTRAINT "disease_detection_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table environment_data
-- ----------------------------
CREATE INDEX "idx_env_orchard" ON "public"."environment_data" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_env_orchard_time" ON "public"."environment_data" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "record_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_env_time" ON "public"."environment_data" USING btree (
  "record_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table environment_data
-- ----------------------------
ALTER TABLE "public"."environment_data" ADD CONSTRAINT "environment_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table farming_calendar
-- ----------------------------
CREATE INDEX "idx_calendar_orchard" ON "public"."farming_calendar" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_calendar_term" ON "public"."farming_calendar" USING btree (
  "solar_term" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table farming_calendar
-- ----------------------------
CREATE TRIGGER "trg_farming_calendar_update" BEFORE UPDATE ON "public"."farming_calendar"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Primary Key structure for table farming_calendar
-- ----------------------------
ALTER TABLE "public"."farming_calendar" ADD CONSTRAINT "farming_calendar_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Triggers structure for table firmware_package
-- ----------------------------
CREATE TRIGGER "trg_firmware_package_update" BEFORE UPDATE ON "public"."firmware_package"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Checks structure for table firmware_package
-- ----------------------------
ALTER TABLE "public"."firmware_package" ADD CONSTRAINT "firmware_package_status_check" CHECK (status = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table firmware_package
-- ----------------------------
ALTER TABLE "public"."firmware_package" ADD CONSTRAINT "firmware_package_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flyway_schema_history
-- ----------------------------
CREATE INDEX "flyway_schema_history_s_idx" ON "public"."flyway_schema_history" USING btree (
  "success" "pg_catalog"."bool_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table flyway_schema_history
-- ----------------------------
ALTER TABLE "public"."flyway_schema_history" ADD CONSTRAINT "flyway_schema_history_pk" PRIMARY KEY ("installed_rank");

-- ----------------------------
-- Indexes structure for table iotda_device
-- ----------------------------
CREATE INDEX "idx_iotda_device_id" ON "public"."iotda_device" USING btree (
  "device_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_iotda_local_device" ON "public"."iotda_device" USING btree (
  "local_device_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table iotda_device
-- ----------------------------
CREATE TRIGGER "trg_iotda_device_update" BEFORE UPDATE ON "public"."iotda_device"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Uniques structure for table iotda_device
-- ----------------------------
ALTER TABLE "public"."iotda_device" ADD CONSTRAINT "iotda_device_device_id_key" UNIQUE ("device_id");

-- ----------------------------
-- Primary Key structure for table iotda_device
-- ----------------------------
ALTER TABLE "public"."iotda_device" ADD CONSTRAINT "iotda_device_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table knowledge_chunk
-- ----------------------------
CREATE INDEX "idx_chunk_doc" ON "public"."knowledge_chunk" USING btree (
  "document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_chunk_embedding" ON "public"."knowledge_chunk" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);
CREATE INDEX "idx_chunk_hash" ON "public"."knowledge_chunk" USING btree (
  "content_hash" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_chunk
-- ----------------------------
ALTER TABLE "public"."knowledge_chunk" ADD CONSTRAINT "knowledge_chunk_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table knowledge_document
-- ----------------------------
CREATE INDEX "idx_doc_category" ON "public"."knowledge_document" USING btree (
  "category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_doc_review" ON "public"."knowledge_document" USING btree (
  "review_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_doc_source" ON "public"."knowledge_document" USING btree (
  "source" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_doc_status" ON "public"."knowledge_document" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table knowledge_document
-- ----------------------------
CREATE TRIGGER "trg_knowledge_document_update" BEFORE UPDATE ON "public"."knowledge_document"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Primary Key structure for table knowledge_document
-- ----------------------------
ALTER TABLE "public"."knowledge_document" ADD CONSTRAINT "knowledge_document_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table orchard
-- ----------------------------
CREATE INDEX "idx_orchard_location" ON "public"."orchard" USING gist (
  "location" "public"."gist_geography_ops"
);
CREATE INDEX "idx_orchard_owner" ON "public"."orchard" USING btree (
  "owner_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table orchard
-- ----------------------------
CREATE TRIGGER "trg_orchard_update" BEFORE UPDATE ON "public"."orchard"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Primary Key structure for table orchard
-- ----------------------------
ALTER TABLE "public"."orchard" ADD CONSTRAINT "orchard_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table orchard_user
-- ----------------------------
CREATE INDEX "idx_ou_user" ON "public"."orchard_user" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table orchard_user
-- ----------------------------
ALTER TABLE "public"."orchard_user" ADD CONSTRAINT "orchard_user_pkey" PRIMARY KEY ("orchard_id", "user_id");

-- ----------------------------
-- Indexes structure for table rag_feedback
-- ----------------------------
CREATE INDEX "idx_rag_fb_session" ON "public"."rag_feedback" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rag_fb_type" ON "public"."rag_feedback" USING btree (
  "feedback_type" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table rag_feedback
-- ----------------------------
ALTER TABLE "public"."rag_feedback" ADD CONSTRAINT "rag_feedback_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table risk_prediction
-- ----------------------------
CREATE INDEX "idx_prediction_level" ON "public"."risk_prediction" USING btree (
  "risk_level" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_prediction_orchard" ON "public"."risk_prediction" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_prediction_time" ON "public"."risk_prediction" USING btree (
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table risk_prediction
-- ----------------------------
ALTER TABLE "public"."risk_prediction" ADD CONSTRAINT "risk_prediction_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table spatial_ref_sys
-- ----------------------------
ALTER TABLE "public"."spatial_ref_sys" ADD CONSTRAINT "spatial_ref_sys_srid_check" CHECK (srid > 0 AND srid <= 998999);

-- ----------------------------
-- Primary Key structure for table spatial_ref_sys
-- ----------------------------
ALTER TABLE "public"."spatial_ref_sys" ADD CONSTRAINT "spatial_ref_sys_pkey" PRIMARY KEY ("srid");

-- ----------------------------
-- Indexes structure for table sys_operation_log
-- ----------------------------
CREATE INDEX "idx_log_action" ON "public"."sys_operation_log" USING btree (
  "action" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_log_time" ON "public"."sys_operation_log" USING btree (
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_log_user" ON "public"."sys_operation_log" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_operation_log
-- ----------------------------
ALTER TABLE "public"."sys_operation_log" ADD CONSTRAINT "sys_operation_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_refresh_token
-- ----------------------------
CREATE INDEX "idx_rt_expires" ON "public"."sys_refresh_token" USING btree (
  "expires_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rt_token" ON "public"."sys_refresh_token" USING btree (
  "token_hash" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rt_token_id" ON "public"."sys_refresh_token" USING btree (
  "token_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rt_user" ON "public"."sys_refresh_token" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table sys_refresh_token
-- ----------------------------
ALTER TABLE "public"."sys_refresh_token" ADD CONSTRAINT "sys_refresh_token_token_id_key" UNIQUE ("token_id");

-- ----------------------------
-- Primary Key structure for table sys_refresh_token
-- ----------------------------
ALTER TABLE "public"."sys_refresh_token" ADD CONSTRAINT "sys_refresh_token_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table sys_role
-- ----------------------------
ALTER TABLE "public"."sys_role" ADD CONSTRAINT "sys_role_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "public"."sys_role" ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_user
-- ----------------------------
CREATE INDEX "idx_user_phone" ON "public"."sys_user" USING btree (
  "phone" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_status" ON "public"."sys_user" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "idx_user_username" ON "public"."sys_user" USING btree (
  "username" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_wechat" ON "public"."sys_user" USING btree (
  "wechat_openid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table sys_user
-- ----------------------------
CREATE TRIGGER "trg_sys_user_update" BEFORE UPDATE ON "public"."sys_user"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_timestamp"();

-- ----------------------------
-- Uniques structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_phone_key" UNIQUE ("phone");
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_wechat_openid_key" UNIQUE ("wechat_openid");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user_role
-- ----------------------------
ALTER TABLE "public"."sys_user_role" ADD CONSTRAINT "sys_user_role_pkey" PRIMARY KEY ("user_id", "role_id");

-- ----------------------------
-- Indexes structure for table user_feedback
-- ----------------------------
CREATE INDEX "idx_fb_created" ON "public"."user_feedback" USING btree (
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_fb_orchard" ON "public"."user_feedback" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_fb_status" ON "public"."user_feedback" USING btree (
  "review_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_fb_type" ON "public"."user_feedback" USING btree (
  "feedback_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_fb_user" ON "public"."user_feedback" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table user_feedback
-- ----------------------------
ALTER TABLE "public"."user_feedback" ADD CONSTRAINT "user_feedback_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table weather_forecast
-- ----------------------------
CREATE INDEX "idx_forecast_fxtime" ON "public"."weather_forecast" USING btree (
  "fxtime" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_forecast_orchard" ON "public"."weather_forecast" USING btree (
  "orchard_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table weather_forecast
-- ----------------------------
ALTER TABLE "public"."weather_forecast" ADD CONSTRAINT "uk_forecast" UNIQUE ("orchard_id", "fxtime", "api_source");

-- ----------------------------
-- Primary Key structure for table weather_forecast
-- ----------------------------
ALTER TABLE "public"."weather_forecast" ADD CONSTRAINT "weather_forecast_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table risk_prediction
-- ----------------------------
ALTER TABLE "public"."risk_prediction" ADD CONSTRAINT "risk_prediction_orchard_id_fkey" FOREIGN KEY ("orchard_id") REFERENCES "public"."orchard" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
