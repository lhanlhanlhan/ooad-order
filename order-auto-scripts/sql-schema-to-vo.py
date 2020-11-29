import re

"""
将 SQL 的字段定义 转为 Vo 对象的属性定义
"""

string = """
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
"""

def formatter(src: str, firstUpper: bool = True):
    """
    将下划线分隔的名字,转换为驼峰模式
    :param src:
    :param firstUpper: 转换后的首字母是否指定大写(如
    :return:
    """
    arr = src.split('_')
    res = ''
    for i in arr:
        res = res + i[0].upper() + i[1:]

    if not firstUpper:
        res = res[0].lower() + res[1:]
    return res


if __name__ == '__main__':
    pattern = re.compile(r'`.*?`')
    fields = re.findall(pattern, string)
    formatted = []
    for idx, field in enumerate(fields):
        field = "#{" + field.replace("`", '') + "}" + ", "
        field = formatter(field, False)
        if (idx+1) % 6 == 0:
            field += "\n"
        formatted.append(field)
    print("VALUES(" + "".join(formatted) + ")")

