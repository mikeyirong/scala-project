create table wish_product (
  id                        integer auto_increment not null,
  product_id                varchar(255),
  name                      varchar(255),
  small_picture             varchar(255),
  keywords                  varchar(255),
  feed_tile_text            varchar(255),
  bought_num                integer,
  rating_star               double,
  rating_num                integer,
  fetch_at                  varchar(255),
  constraint pk_wish_product primary key (id))
;

create table wish_product_variation (
  id                        integer auto_increment not null,
  variation_id              varchar(255),
  original_price            varchar(255),
  wish_product_id           integer,
  shipping_price_country_code varchar(255),
  is_fulfill_by_wish        tinyint(1) default 0,
  is_fulfill_by_wlc         tinyint(1) default 0,
  color                     varchar(255),
  size_ordering             varchar(255),
  min_fulfillment_time      varchar(255),
  max_shipping_time         varchar(255),
  price                     varchar(255),
  inventory                 varchar(255),
  constraint pk_wish_product_variation primary key (id))
;

alter table wish_product_variation add constraint fk_wish_product_variation_wish_product_1 foreign key (wish_product_id) references wish_product (id) on delete restrict on update restrict;
create index ix_wish_product_variation_wish_product_1 on wish_product_variation (wish_product_id);


