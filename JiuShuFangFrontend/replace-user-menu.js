const fs = require('fs');
const path = require('path');

const files = [
  'MyShop.vue', 'OrderDetail.vue', 'Comment.vue', 'ShoppingCart.vue', 
  'UploadGoods.vue', 'ProductDetail.vue', 'Profile.vue', 'SearchResults.vue', 
  'ChatDetail.vue', 'AddressPage.vue', 'Favorites.vue', 'History.vue', 
  'ChatList.vue', 'EditProfile.vue', 'MyComments.vue', 'EditComment.vue', 
  'OrderCheckout.vue', 'ChangePassword.vue'
];

const oldPattern = /<div class="user-profile">[\s\S]*?<\/el-popover>[\s\S]*?<\/div>/;
const newComponent = '<UserMenu :userInfo="userInfo" @logout="handleLogout" />';

files.forEach(file => {
  const filePath = path.join(__dirname, file);
  if (!fs.existsSync(filePath)) return;
  
  let content = fs.readFileSync(filePath, 'utf8');
  
  // 替换模板部分
  content = content.replace(oldPattern, newComponent);
  
  // 检查是否已有 UserMenu import
  if (!content.includes("import UserMenu from")) {
    // 在 script setup 中添加 UserMenu import
    content = content.replace(
      /(<script setup>)/,
      `$1\nimport UserMenu from './components/UserMenu.vue'`
    );
  }
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log(`Updated: ${file}`);
});

console.log('Done!');
