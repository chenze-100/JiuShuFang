package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.domain.dto.SetPasswordDto;
import com.JiuShuFangSecondHand.domain.pojo.User;
import com.JiuShuFangSecondHand.domain.pojo.WechatUser;
import com.JiuShuFangSecondHand.domain.vo.ChatMessageVO;
import com.JiuShuFangSecondHand.domain.vo.UserLoginVO;
import com.JiuShuFangSecondHand.domain.vo.UserVO;
import com.JiuShuFangSecondHand.enums.CodeType;
import com.JiuShuFangSecondHand.enums.RedisLoginStatus;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.properties.ServerProperties;
import com.JiuShuFangSecondHand.mapper.UserMapper;
import com.JiuShuFangSecondHand.service.IUserService;
import com.JiuShuFangSecondHand.util.Result;
import com.JiuShuFangSecondHand.util.UserContext;
import com.JiuShuFangSecondHand.util.WxCodeUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.JiuShuFangSecondHand.util.JwtUtil;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @since 2026-04-07
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StringRedisTemplate redisTemplate;
    private final ServerProperties serverConfig;

    @Override
    public Result<UserLoginVO> LoginCheck(String phone, String msg, Integer type) {
        System.out.println("开始验证登录");
        UserLoginVO userLoginVO;
        //用户名密码登录
        if (type == 0) {
            userLoginVO = passwordLogin(phone, msg);
        } else {
            //短信验证码登录
            userLoginVO = smsLogin(phone, msg);
        }
        //登录成功将
        return Result.success(userLoginVO);
    }

    @Override
    public Result<UserLoginVO> QRCodeLogin(String code, String loginId) {
        UserLoginVO userLoginVO = null;
        if (code != null) {
            userLoginVO = WechatSendLogin(code, loginId);
        } else {
            userLoginVO = SendLogin(loginId);
        }
        return Result.success(userLoginVO);
    }

    @Override
    public Result<UserLoginVO> confirmLogin(String loginId) {
        String userInfoStr = redisTemplate.opsForValue().get(RedisConstant.LOGIN_USERINFO_KEY + loginId);
        if (userInfoStr == null) {
            throw new RuntimeException("登录信息已过期");
        }
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_ID_KEY + loginId,
                RedisLoginStatus.LOGIN_SUCCESS.getCode().toString(), 50, TimeUnit.SECONDS);
        UserLoginVO userLoginVO = JSONUtil.toBean(userInfoStr, UserLoginVO.class);
        return Result.success(userLoginVO);
    }

    private UserLoginVO SendLogin(String loginId) {
        String userInfoStr = redisTemplate.opsForValue().get(RedisConstant.LOGIN_USERINFO_KEY + loginId);
        return JSONUtil.toBean(userInfoStr, UserLoginVO.class);

    }

    private UserLoginVO WechatSendLogin(String code, String loginId) {
        String s = redisTemplate.opsForValue().get(RedisConstant.LOGIN_ID_KEY + loginId);
        if (s == null) {
            throw new RuntimeException("二维码已失效");
        }
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_ID_KEY + loginId,
                RedisLoginStatus.LOGINING.getCode().toString(), 50, TimeUnit.SECONDS);
        WechatUser wxUser = WxCodeUtil.getWxCode(code);
        User user = lambdaQuery().eq(User::getOpenid, wxUser.getOpenid()).one();
        if (user == null) {
            user = new User();
            user.setNickName(wxUser.getNickname());
            user.setAvatar(wxUser.getHeadimgurl());
            user.setOpenid(wxUser.getOpenid());
            user.setSex(wxUser.getSex());
            user.setLoginType(2);
            user.setPassword(BCrypt.hashpw(UUID.randomUUID() + wxUser.getOpenid()));
            save(user);
        }
        UserLoginVO userLoginVO = getUserLoginVO(user);
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_USERINFO_KEY + loginId,
                JSONUtil.toJsonStr(userLoginVO), 50, TimeUnit.SECONDS);
        return userLoginVO;
    }

    @Override
    public void loginCheck(HttpServletResponse response) {
        String loginId = IdUtil.simpleUUID();
        String wait = RedisLoginStatus.WAIT.getCode().toString();
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_ID_KEY + loginId,
                wait, 50, TimeUnit.SECONDS);
        response.addHeader("loginId", loginId);
        String redirectUri = URLEncoder.encode(
                serverConfig.getExternalUrl() + "/user/login/check/QRCode" + "?loginId=" + loginId,
                StandardCharsets.UTF_8
        );
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=wxaca57064625d7156" +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=snsapi_userinfo" +
                "&state=test" +
                "#wechat_redirect";

        response.setContentType("image/png");
        try {
            QrCodeUtil.generate(url, 180, 180, "png", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<String> check(String loginId) {
        String data = redisTemplate.opsForValue().get(RedisConstant.LOGIN_ID_KEY + loginId);
        return Result.success(data);
    }

    /**
     * 短信验证码登录
     * @param phone
     * @param msg
     */
    private UserLoginVO smsLogin(String phone, String msg) {
        String key = RedisConstant.CODE_SEND_KEY + phone
                + ":" + CodeType.LOGIN_VERIFY.getCode();
        String code = redisTemplate.opsForValue().get(key);
        if (code == null || !code.equals(msg)) {
            throw new RuntimeException("短信验证码错误");
        }
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setLoginType(1);
            user.setNickName(UUID.fastUUID().toString(true));
            user.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
            save(user);
        }
        return getUserLoginVO(user);
    }


    /**
     * 密码登录
     * @param phone
     * @param password
     * @return
     */
    private UserLoginVO passwordLogin(String phone, String password) {
        if (phone == null || password == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        System.out.println(user);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("用户名："+phone+"密码："+password);
            throw new RuntimeException("用户名或密码错误");
        }
        return getUserLoginVO(user);
    }

    @NotNull
    public UserLoginVO getUserLoginVO(User user) {
        UserLoginVO userLoginVO = BeanUtil.copyProperties(user, UserLoginVO.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        String token = JwtUtil.createToken(map, user.getNickName());

        userLoginVO.setToken(token);
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_TOKEN_KEY + user.getId(),
                token, 30, TimeUnit.MINUTES);
        return userLoginVO;
    }

    @Override
    public Result forgetPassword(SetPasswordDto dto) {
        String key = RedisConstant.LOGIN_CODE_KEY + dto.getPhone()
                + ":" + CodeType.FORGET_PASSWORD.getCode();
        setPasswordByCode(dto, key);
        return Result.success();
    }

    @Override
    public UserVO getUserInfoById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }


    @Override
    public Result updateUserSimpleById(User user) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        User dbUser = getById(userId);
        if (dbUser == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(userId);
        updateById(user);
        return Result.success();
    }

    @Override
    public Result updatePassword(SetPasswordDto dto) {
        String key = RedisConstant.LOGIN_CODE_KEY + dto.getPhone()
                + ":" + CodeType.CHANGE_PASSWORD.getCode();
        setPasswordByCode(dto, key);
        return Result.success();
    }

    @Override
    public Result<Double> getUserBalance() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.success(user.getBalance().doubleValue());
    }

    @Override
    public void setUserInfoInSession(ChatMessageVO chatMessageVO) {
        String key = RedisConstant.CHAT_SESSION_KEY + chatMessageVO.getFromId();
        String name = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(name)) {
            chatMessageVO.setFromNickName(name);
        }
        User user = getById(chatMessageVO.getFromId());
        if(user == null){
            log.error("用户不存在");
            return;
        }
        chatMessageVO.setFromNickName(user.getNickName());
        redisTemplate.opsForValue()
                .set(key, user.getNickName(), 10, TimeUnit.MINUTES);
    }

    private void setPasswordByCode(SetPasswordDto dto, String key) {
        String redisCode = redisTemplate.opsForValue().get(key);
        if (redisCode == null || !redisCode.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        String encryptedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        update(new UpdateWrapper<User>()
                .eq("phone", dto.getPhone())
                .set("password", encryptedPassword));
    }
}
