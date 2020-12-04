-- 定义一些 INDEX

-- 重量模板，同一模板不能多个 region
alter table `weight_freight_model` add unique index(freight_model_id, region_id);

-- 件数模板，同一模板不能多个 region
alter table `piece_freight_model` add unique index(freight_model_id, region_id);
