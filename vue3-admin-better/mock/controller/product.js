module.exports = [
  {
    url: "/product/list",
    type: "get",
    response: () => {
      return {
        code: 200,
        msg: "获取商品列表成功",
        data: {
          records: [
            {
              id: 1,
              name: "皇家猫粮",
              category: "猫粮",
              price: 299.00,
              stock: 100,
              description: "高品质猫粮，营养丰富",
              image: "product1.jpg",
              imageUrl: "http://localhost:8080/uploads/product/product1.jpg",
              status: "上架",
              createTime: "2024-01-01 10:00:00",
              updateTime: "2024-01-01 10:00:00"
            },
            {
              id: 2,
              name: "狗狗玩具",
              category: "玩具",
              price: 89.00,
              stock: 50,
              description: "耐咬狗狗玩具",
              image: "product2.jpg",
              imageUrl: "http://localhost:8080/uploads/product/product2.jpg",
              status: "上架",
              createTime: "2024-01-02 10:00:00",
              updateTime: "2024-01-02 10:00:00"
            },
            {
              id: 3,
              name: "宠物洗护用品",
              category: "洗护",
              price: 158.00,
              stock: 30,
              description: "温和宠物洗护用品",
              image: "product3.jpg",
              imageUrl: "http://localhost:8080/uploads/product/product3.jpg",
              status: "上架",
              createTime: "2024-01-03 10:00:00",
              updateTime: "2024-01-03 10:00:00"
            }
          ],
          total: 3,
          size: 10,
          current: 1,
          pages: 1
        }
      };
    }
  },
  {
    url: "/product/categories",
    type: "get",
    response: () => {
      return {
        code: 200,
        msg: "获取分类列表成功",
        data: [
          { id: 1, name: "猫粮" },
          { id: 2, name: "玩具" },
          { id: 3, name: "洗护" }
        ]
      };
    }
  },
  {
    url: "/product/\\d+",
    type: "get",
    response: (req) => {
      const id = req.url.split('/').pop();
      return {
        code: 200,
        msg: "获取商品详情成功",
        data: {
          id: parseInt(id),
          name: "商品详情",
          category: "分类",
          price: 99.00,
          stock: 100,
          description: "商品描述",
          image: "product.jpg",
          imageUrl: "http://localhost:8080/uploads/product/product.jpg",
          status: "上架"
        }
      };
    }
  }
];
