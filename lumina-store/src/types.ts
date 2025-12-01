export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}
export interface IndexSlider {
  id: number;
  url: string;
  title: string;
  subtitle: string;
  cta: string;
  link: string;
}

export interface OfficialCollection {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
}

export interface Address {
  id: number;
  userId: number;
  consignee: string;
  consigneeAddress: string;
  consigneePhone: string;
  isDefault: number;
}


export interface User {
  id: number;
  username: string;
  nickname: string;
  email: string;
  avatar: string;
  sex: number;
  mobile: string;
  signature: string;
  token: string;
}

export interface Comment {
  id: string;
  userId: string;
  userName: string;
  userAvatar: string;
  content: string;
  date: string;
}
export interface CommentView {
  id: number;
  pid: number;
  fid: number;
  module: string;
  rootId: number;
  userId: number;
  createTime: string;
  updateTime: string;
  comment: string;
  username: string;
  userAvatar: string;
  parentUserName: string;
  children: CommentView[];
}

export interface Product {
  id: number;
  userId: number;
  name: string;
  categoryId: number;
  brandId: number;
  price: number;
  inventory: number;
  image: string;
  detailImages: string; //JSON数组
  description: string;
  descriptionImage: string; //JSON数组
  label: string;
}
export interface ProductView {
  id: number;
  name: string;
  categoryName: string;
  brandName: number;
  price: number;
  inventory: number;
  image: string;
  detailImages: string; //JSON数组
  description: string;
  descriptionImage: string; //JSON数组
  label: string;
}

export interface Brand {
  id: number;
  name: string;
}
export interface Category {
  id: number;
  name: string;
}
export interface Blog {
  id: number;
  userId: number;
  author: string;
  title: string;
  content: string;
  contentMd: string;
  image: string;
  aiDescribe: string;
  createTime: string;
  userAvatar: string;
  status: number;
  signature: string;
}

export interface Cart {
  id: number;
  userId: number;
  productId: number;
  number: number;
  productName: string;
  productPrice: number;
  productImage: string;
}
export interface Favorites {
  id: number;
  userId: number;
  productId: number;
  productName: string;
  productPrice: number;
  productImage: string;
}

export interface Order {
  id: number;
  total: number;
  date: string;
  status: 'Processing' | 'Shipped' | 'Delivered';
  shippingAddress: string;
}

export interface Captcha {
  nonceStr: string;
  value: string;
  canvasSrc: string;
  canvasWidth: number;
  canvasHeight: number;
  blockSrc: string;
  blockWidth: number;
  blockHeight: number;
  blockRadius: number;
  blockX: number;
  blockY: number;
}

export interface SysConfig {
  id: number;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;

}