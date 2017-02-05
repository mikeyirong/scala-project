create table wish_product (
  id                        varchar(255) not null,
  small_picture             varchar(255),
  feed_tile_text            varchar(255),
  bought_num                integer,
  rating_star               double,
  rating_num                integer,
  constraint pk_wish_product primary key (id))
;



