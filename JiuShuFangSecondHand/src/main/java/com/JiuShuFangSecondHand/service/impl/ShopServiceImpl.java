package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.JiuShuFangSecondHand.domain.dto.ShopAddDto;
import com.JiuShuFangSecondHand.domain.pojo.Shop;
import com.JiuShuFangSecondHand.domain.pojo.ShopAuditLog;
import com.JiuShuFangSecondHand.domain.pojo.User;
import com.JiuShuFangSecondHand.domain.vo.ShopVO;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.mapper.ShopAuditLogMapper;
import com.JiuShuFangSecondHand.mapper.ShopMapper;
import com.JiuShuFangSecondHand.mapper.UserMapper;
import com.JiuShuFangSecondHand.service.IShopService;
import com.JiuShuFangSecondHand.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商铺服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements IShopService {

    private final ShopMapper shopMapper;
    private final ShopAuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

    /** 每人最多开设商铺数 */
    private static final int MAX_SHOP_COUNT = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addShop(ShopAddDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        // ====== 流程图：是否已完成实名认证 ======
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRealNameStatus() == null || user.getRealNameStatus() != 1) {
            throw new BusinessException("请先完成实名认证");
        }

        // ====== 流程图：商铺数量是否已达上限 ======
        int shopCount = Math.toIntExact(shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getUserId, userId)
                        .eq(Shop::getIsDeleted, 0)
        ));
        if (shopCount >= MAX_SHOP_COUNT) {
            throw new BusinessException("每人最多开设 " + MAX_SHOP_COUNT + " 家商铺");
        }

        // ====== 流程图：是否存在审核中的商铺 ======
        boolean hasPending = shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getUserId, userId)
                        .eq(Shop::getStatus, Shop.StatusEnum.PENDING.getCode())
                        .eq(Shop::getIsDeleted, 0)
        ) > 0;
        if (hasPending) {
            throw new BusinessException("有商铺正在审核，请耐心等待");
        }

        // ====== 后端再次校验：重复名称 ======
        Long dupCount = shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getName, dto.getName())
                        .eq(Shop::getIsDeleted, 0)
        );
        if (dupCount != null && dupCount > 0) {
            throw new BusinessException("商铺名称已被占用");
        }

        // ====== 后端再次校验：敏感词（简单示例，实际可接入敏感词库）======
        String sensitive = checkSensitiveWord(dto.getName(), dto.getIntro());
        if (sensitive != null) {
            throw new BusinessException("商铺信息包含敏感词：" + sensitive);
        }

        // ====== 写入数据库 ======
        Shop shop = new Shop();
        BeanUtils.copyProperties(dto, shop);
        shop.setUserId(userId);
        shop.setStatus(Shop.StatusEnum.PENDING.getCode());
        shop.setIsDeleted(0);
        shopMapper.insert(shop);

        // ====== 写入审核日志 ======
        ShopAuditLog auditLog = new ShopAuditLog();
        auditLog.setShopId(shop.getId());
        auditLog.setOperatorId(userId); // 申请人即操作人
        auditLog.setAction(ShopAuditLog.ActionEnum.CREATE.getCode());
        auditLog.setReason("用户申请开设商铺");
        auditLogMapper.insert(auditLog);

        log.info("商铺创建成功，userId={}, shopId={}, name={}", userId, shop.getId(), dto.getName());
        return shop.getId();
    }

    @Override
    public List<ShopVO> listMyShops() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getUserId, userId)
                        .eq(Shop::getIsDeleted, 0)
                        .orderByDesc(Shop::getCreateTime)
        );
        return shops.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ShopVO getById(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getIsDeleted() == 1) {
            throw new BusinessException("商铺不存在");
        }
        return toVO(shop);
    }

    @Override
    public boolean isRealNameVerified() {
        System.out.println("检查实名认证状态");
        System.out.println("userId=" + UserContext.getUserId());
        Long userId = UserContext.getUserId();

        if (userId == null) return false;
        User user = userMapper.selectById(userId);
        return user != null && user.getRealNameStatus() != null && user.getRealNameStatus() == 1;
    }

    @Override
    public boolean hasPendingShop() {
        System.out.println("检查是否有审核中的商铺");
        Long userId = UserContext.getUserId();
        if (userId == null) return false;
        return shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getUserId, userId)
                        .eq(Shop::getStatus, Shop.StatusEnum.PENDING.getCode())
                        .eq(Shop::getIsDeleted, 0)
        ) > 0;
    }

    @Override
    public int countMyShops() {
        System.out.println("检查我的商铺数量");
        Long userId = UserContext.getUserId();
        if (userId == null) return 0;
        return Math.toIntExact(shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getUserId, userId)
                        .eq(Shop::getIsDeleted, 0)
        ));
    }

    /**
     * 简单敏感词检查（示例）
     */
    private String checkSensitiveWord(String... texts) {
        // TODO: 接入真正的敏感词库
        String[] sensitiveWords = {"诈骗", "赌博", "色情", "毒品"};
        for (String text : texts) {
            if (StrUtil.isBlank(text)) continue;
            for (String word : sensitiveWords) {
                if (text.contains(word)) return word;
            }
        }
        return null;
    }

    private ShopVO toVO(Shop shop) {
        ShopVO vo = new ShopVO();
        BeanUtils.copyProperties(shop, vo);
        Shop.StatusEnum statusEnum = Shop.StatusEnum.getByCode(shop.getStatus());
        if (statusEnum != null) {
            vo.setStatusDesc(statusEnum.getDesc());
        }
        return vo;
    }
}
