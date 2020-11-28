import re
import random
import time

from dictionary import WordDict

schema = """
CREATE TABLE `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customer_id` bigint(20) DEFAULT NULL,
  `shop_id` bigint(20) DEFAULT NULL,
  `order_sn` varchar(128) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `consignee` varchar(64) DEFAULT NULL,
  `region_id` bigint(20) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `mobile` varchar(128) DEFAULT NULL,
  `message` varchar(500) DEFAULT NULL,
  `order_type` tinyint(4) DEFAULT NULL,
  `freight_price` bigint(10) DEFAULT NULL,
  `coupon_id` bigint(20) DEFAULT NULL,
  `coupon_activity_id` bigint(20) DEFAULT NULL,
  `discount_price` bigint(10) DEFAULT NULL,
  `origin_price` bigint(10) DEFAULT NULL,
  `presale_id` bigint(20) DEFAULT NULL,
  `groupon_discount` bigint(10) DEFAULT NULL,
  `rebate_num` int(11) DEFAULT NULL,
  `confirm_time` datetime DEFAULT NULL,
  `shipment_sn` varchar(128) DEFAULT NULL,
  `state` tinyint(4) DEFAULT NULL,
  `substate` tinyint(4) DEFAULT NULL,
  `be_deleted` tinyint(4) DEFAULT NULL,
  `gmt_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT NULL,
)
"""
count = 50
fields = ['id', 'customer_id', 'shop_id', 'order_sn',
          'consignee', 'region_id', 'address', 'mobile',
          'message', 'order_type',
          'freight_price', 'coupon_id', 'coupon_activity_id',
          ]
word_dict = WordDict()


# 请先运行这个，获得字典
def gen_dict():
    pattern = re.compile(r'`.+`')
    f = re.findall(pattern, schema)
    print("{")
    for fi in f:
        print("'" + fi + "': ")
    print("}")


# 填写字典后，然后再运行这个，获得数据
def gen_text():
    for _ in range(count):
        # 新建一组数据
        normal_data_dict = {
            '`customer_id`': 1,
            '`shop_id`': random.randint(1, 10),
            '`order_sn`': word_dict.create_str(10),
            '`consignee`': word_dict.create_word(2),
            '`region_id`': 2,
            '`address`': word_dict.create_word(10),
            '`mobile`': '139131231',
            '`message`': word_dict.create_word(20),
            '`order_type`': 0,
            '`freight_price`': random.randint(800, 1000),
            '`discount_price`': 0,
            '`origin_price`': random.randint(1200, 23000),
            '`rebate_num`': 0,
            '`confirm_time`': '2020-11-25',
            '`shipment_sn`': word_dict.create_str(10),
            '`state`': random.randint(1, 15),
            '`be_deleted`': 0,
            # 2020-9-25 10:42:34 - 2020-11-25 10:42:34
            '`gmt_created`': time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(random.randint(1595644954, 1606272154)))
        }

        keys = []
        values = []
        for key, val in normal_data_dict.items():
            keys.append(key)
            if isinstance(val, int):
                values.append("{}".format(val))
            elif isinstance(val, str):
                values.append("'{}'".format(val))

        keys = ", ".join(keys)
        values = ", ".join(values)
        sql = "INSERT INTO `order` (" + keys + ") VALUES (" + values + ");\n"

        # 输出至文件
        with open('testdata.sql', 'at') as f:
            f.write(sql)

    # presale_dict = {
    #     '`customer_id`': 1,
    #     '`shop_id`': random.randint(1, 10),
    #     '`order_sn`': uuid.uuid1().get_hex(),
    #     '`consignee`': word_dict.create_word(2),
    #     '`region_id`': 2,
    #     '`address`': word_dict.create_word(10),
    #     '`mobile`': '139131231',
    #     '`message`': word_dict.create_word(20),
    #     '`order_type`': 0,
    #     '`freight_price`': random.randint(800, 1000),
    #     '`discount_price`': 0,
    #     '`origin_price`': random.randint(1200, 23000),
    #     '`presale_id`':
    #     '`groupon_discount`':
    #     '`rebate_num`':
    #     '`confirm_time`':
    #     '`shipment_sn`':
    #     '`state`':
    #     '`substate`':
    #     '`be_deleted`':
    #     '`gmt_created`':
    #     '`gmt_modified`':
    # }



if __name__ == '__main__':
    # gen_dict()
    gen_text()
