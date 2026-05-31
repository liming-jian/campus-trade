package com.campus_trade.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.entity.Admin;
import com.campus_trade.entity.User;
import com.campus_trade.entity.Product;
import com.campus_trade.entity.ProductImage;
import com.campus_trade.mapper.AdminMapper;
import com.campus_trade.mapper.UserMapper;
import com.campus_trade.mapper.ProductMapper;
import com.campus_trade.mapper.ProductImageMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminMapper adminMapper, UserMapper userMapper,
                          ProductMapper productMapper, ProductImageMapper productImageMapper,
                          PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LambdaQueryWrapper<Admin> adminW = new LambdaQueryWrapper<>();
        adminW.eq(Admin::getUsername, "admin");
        Admin admin = adminMapper.selectOne(adminW);
        if (admin == null) {
            admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setRole("ADMIN");
            adminMapper.insert(admin);
            System.out.println("====== 管理员账号: admin / admin123 ======");
        } else {
            String encodedPwd = passwordEncoder.encode("admin123");
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                admin.setPassword(encodedPwd);
                adminMapper.updateById(admin);
                System.out.println("====== 管理员密码已重置: admin / admin123 ======");
            }
        }

        LambdaQueryWrapper<User> userW = new LambdaQueryWrapper<>();
        userW.eq(User::getPhone, "13800000000");
        User seller = userMapper.selectOne(userW);
        Long sellerId;
        if (seller == null) {
            seller = new User();
            seller.setPhone("13800000000");
            seller.setPassword(passwordEncoder.encode("Xy123456!"));
            seller.setNickname("校园卖家");
            seller.setSchoolName("清华大学");
            seller.setVerifyStatus(2);
            seller.setStatus(1);
            userMapper.insert(seller);
            sellerId = seller.getId();
        } else {
            seller.setPassword(passwordEncoder.encode("Xy123456!"));
            seller.setSchoolName("清华大学");
            seller.setVerifyStatus(2);
            userMapper.updateById(seller);
            sellerId = seller.getId();
        }
        System.out.println("====== 默认卖家: 13800000000 / Xy123456! ======");

        LambdaQueryWrapper<Product> oldProdW = new LambdaQueryWrapper<>();
        oldProdW.eq(Product::getUserId, sellerId);
        productMapper.delete(oldProdW);

        Object[][] items = {
                {"iPhone 13 128G 午夜色", "自用iPhone13，128G版本，无拆无修，一直贴膜带壳使用。边框有轻微使用痕迹，屏幕完美。配件齐全含原装充电线。支持当面验货。", "3799", 1L, "NEW", "/sample-products/iphone-13-midnight.png"},
                {"高等数学第七版 同济大学", "考研用书，同济版高数上下册。书内少量笔记，整体8成新。课后习题答案齐全，适合考研复习使用。买就送配套习题全解一本。", "25", 2L, "USED", "/sample-products/advanced-math-7.png"},
                {"MacBook Pro 2021 M1 Pro", "M1 Pro芯片，16G内存，512G存储。95新，电池循环仅80次。原装充电器+包装盒齐全。因换新机出售，可小刀。", "8500", 1L, "NEW", "/sample-products/macbook-pro-2021.png"},
                {"宿舍折叠椅 人体工学", "宿舍用人体工学椅，可折叠不占空间。用了半年，腰部支撑很好，久坐不累。马上毕业了带不走，低价转让。", "120", 7L, "USED", "/sample-products/folding-chair.png"},
                {"Nike Air Force 1 白色 42码", "正品空军一号，穿了两三次，几乎全新。42码偏小半码。买大了所以出，保真正品支持验货。", "399", 5L, "NEW", "/sample-products/nike-air-force-1.png"},
                {"四级词汇闪过 全新未拆封", "买了没看全新未拆的四级词汇书，送真题册。价格就是书费的一半，需要的同学来。还可以顺便分享备考经验哈哈。", "15", 2L, "NEW", "/sample-products/cet4-vocabulary.png"},
                {"机械键盘 IKBC C87 青轴", "ikbc c87机械键盘，樱桃青轴，打字很爽。用了大概一年，键帽轻微打油，功能一切正常。换无线键盘了所以出掉。", "150", 1L, "USED", "/sample-products/ikbc-keyboard.png"},
                {"宿舍用小冰箱 迷你款", "宿舍用迷你小冰箱，4L容量放饮料水果够用。功率小不跳闸，制冷效果不错。毕业甩卖，先到先得。", "80", 7L, "OLD", "/sample-products/mini-fridge.png"},
                {"兰蔻持妆粉底液 PO-01", "兰蔻持妆粉底液，色号PO-01。只用过三四次，色号不适合我。专柜390买的，便宜出。", "180", 4L, "NEW", "/sample-products/lancome-foundation.png"},
                {"考研英语一 张剑黄皮书", "全套张剑黄皮书，真题+解析都有。认真做过一遍，笔记工整。附送作文模板和网课讲义，考研党必备。", "35", 2L, "USED", "/sample-products/kaoyan-english-yellow-book.png"},
        };

        for (Object[] item : items) {
            Product p = new Product();
            p.setUserId(sellerId);
            p.setCategoryId((Long) item[3]);
            p.setTitle((String) item[0]);
            p.setDescription((String) item[1]);
            p.setPrice(new BigDecimal((String) item[2]));
            p.setCondition((String) item[4]);
            p.setShippingFree(1);
            p.setShippingFee(BigDecimal.ZERO);
            p.setStatus("PASS");
            p.setViewCount((int) (Math.random() * 200));
            p.setFavoriteCount((int) (Math.random() * 30));
            productMapper.insert(p);

            ProductImage img = new ProductImage();
            img.setProductId(p.getId());
            img.setUrl((String) item[5]);
            img.setSortOrder(0);
            productImageMapper.insert(img);
        }
        System.out.println("====== 已生成 " + items.length + " 件示例商品 ======");
    }
}
