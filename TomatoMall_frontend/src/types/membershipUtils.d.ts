// TypeScript 声明文件 for membershipUtils.js
declare module '../../utils/membershipUtils' {
  export const MEMBERSHIP_CARD_IDS: Record<string, number>
  export function isMembershipCard(productId: string): boolean
  export function getMemberLevelByProductId(productId: string): number
  export function getDiscountRateByLevel(level: number): number
  export function getMemberLevelName(level: number): string
}
