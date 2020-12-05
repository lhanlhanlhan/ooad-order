-- 定义一些 INDEX

-- 重量模板，同一模板不能多个 region
ALTER TABLE `weight_freight_model` ADD UNIQUE INDEX(`freight_model_id`, `region_id`);

-- 件数模板，同一模板不能多个 region
ALTER TABLE `piece_freight_model` ADD UNIQUE INDEX(`freight_model_id`, `region_id`);

-- order sn 上的唯一索引
ALTER TABLE `orders` ADD UNIQUE INDEX(`order_sn`);

-- freight model name 上的唯一索引
ALTER TABLE `freight_model` ADD UNIQUE INDEX(`name`);
