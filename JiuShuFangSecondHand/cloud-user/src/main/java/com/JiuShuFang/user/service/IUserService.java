package com.JiuShuFang.user.service;

import com.JiuShuFang.user.domain.dto.SetPasswordDto;
import com.JiuShuFang.user.domain.pojo.User;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.user.domain.vo.UserLoginVO;
import com.JiuShuFang.user.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 *  用户服务类
 * </p>
 *
 * @since 2026-04-07
 */
public interface IUserService extends IService<User> {


    /**
     * 根据手机号获取用户信息
     *
     * @param phone
     * @param msg
     * @param type
     * @return
     */
    Result<UserLoginVO> LoginCheck(String phone, String msg, Integer type);

    Result<UserLoginVO> QRCodeLogin(String code, String session);

    void loginCheck(HttpServletResponse response);

    Result<String> check(String loginId);

    Result<UserLoginVO> confirmLogin(String loginId);

    Result forgetPassword(SetPasswordDto dto);

    UserVO getUserInfoById(Long id);


    Result updateUserSimpleById(User user);

    Result updatePassword(SetPasswordDto dto);

    Result<Double> getUserBalance();

    // 微服务拆分：该方法依赖 AI 服务的 ChatMessageVO，第一阶段注释，后续改由 Feign 调用 cloud-ai
    // void setUserInfoInSession(ChatMessageVO chatMessageVO);
}
