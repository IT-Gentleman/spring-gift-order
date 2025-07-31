-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS gift_order;
DROP TABLE IF EXISTS wish;
DROP TABLE IF EXISTS kakao_token;
DROP TABLE IF EXISTS product_option;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS member;

CREATE TABLE gift_order (id BIGINT AUTO_INCREMENT NOT NULL, product_option_id BIGINT NOT NULL, sender_member_id BIGINT NOT NULL, receiver_member_id BIGINT NOT NULL, quantity INT NOT NULL, order_date_time datetime NULL, message VARCHAR(255) NOT NULL, CONSTRAINT pk_gift_order PRIMARY KEY (id));

CREATE TABLE kakao_token (id BIGINT NOT NULL, access_token TEXT NOT NULL, refresh_token TEXT NOT NULL, CONSTRAINT pk_kakao_token PRIMARY KEY (id));

CREATE TABLE member (id BIGINT AUTO_INCREMENT NOT NULL, deleted_at datetime NULL, created_at datetime NULL, updated_at datetime NULL, created_by_id BIGINT NULL, updated_by_id BIGINT NULL, email VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, `role` SMALLINT NOT NULL, kakao_id BIGINT NULL, CONSTRAINT pk_member PRIMARY KEY (id));

CREATE TABLE product (id BIGINT AUTO_INCREMENT NOT NULL, deleted_at datetime NULL, created_at datetime NULL, updated_at datetime NULL, created_by_id BIGINT NULL, updated_by_id BIGINT NULL, name VARCHAR(255) NOT NULL, price INT NOT NULL, image_url VARCHAR(255) NOT NULL, validated BIT(1) NOT NULL, CONSTRAINT pk_product PRIMARY KEY (id));

CREATE TABLE product_option (id BIGINT AUTO_INCREMENT NOT NULL, created_at datetime NULL, updated_at datetime NULL, created_by_id BIGINT NULL, updated_by_id BIGINT NULL, name VARCHAR(255) NOT NULL, quantity INT NOT NULL, product_id BIGINT NOT NULL, CONSTRAINT pk_productoption PRIMARY KEY (id));

CREATE TABLE wish (id BIGINT AUTO_INCREMENT NOT NULL, created_at datetime NULL, updated_at datetime NULL, created_by_id BIGINT NULL, updated_by_id BIGINT NULL, member_id BIGINT NOT NULL, product_id BIGINT NOT NULL, CONSTRAINT pk_wish PRIMARY KEY (id));

ALTER TABLE product_option ADD CONSTRAINT uc_d28425939ff7df66e2351f3be UNIQUE (product_id, name);

ALTER TABLE member ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE member ADD CONSTRAINT uc_member_kakaoid UNIQUE (kakao_id);

ALTER TABLE gift_order ADD CONSTRAINT FK_GIFT_ORDER_ON_PRODUCT_OPTION FOREIGN KEY (product_option_id) REFERENCES product_option (id);

ALTER TABLE kakao_token ADD CONSTRAINT FK_KAKAO_TOKEN_ON_ID FOREIGN KEY (id) REFERENCES member (id);

ALTER TABLE product_option ADD CONSTRAINT FK_PRODUCTOPTION_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE wish ADD CONSTRAINT FK_WISH_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE wish ADD CONSTRAINT FK_WISH_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);