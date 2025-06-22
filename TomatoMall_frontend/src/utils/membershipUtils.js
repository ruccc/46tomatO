// 会员卡商品ID常量
export const MEMBERSHIP_CARD_IDS = {
  // 一级会员卡 (9折)
  'dcaf11e7-c1a8-42c7-a51e-bc6ddb1f2628': 1,
  // 二级会员卡 (8折)
  '60300a88-8def-4dad-ba5d-6bbb668e6f27': 2,
  // 三级会员卡 (7折)
  'fc3eb463-913d-4645-bff5-a3607a901e43': 3
};

// 判断商品是否为会员卡
export const isMembershipCard = (productId) => {
  return Object.keys(MEMBERSHIP_CARD_IDS).includes(productId);
};

// 根据商品ID获取会员等级
export const getMemberLevelByProductId = (productId) => {
  return MEMBERSHIP_CARD_IDS[productId] || 0;
};

// 获取会员等级折扣率
export const getDiscountRateByLevel = (level) => {
  switch (level) {
    case 1: return 0.9; // 9折
    case 2: return 0.8; // 8折
    case 3: return 0.7; // 7折
    default: return 1.0; // 无折扣
  }
};

// 获取会员等级名称
export const getMemberLevelName = (level) => {
  switch (level) {
    case 1: return '一级会员';
    case 2: return '二级会员';
    case 3: return '三级会员';
    default: return '普通用户';
  }
};
