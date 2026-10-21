package com.JiuShuFangSecondHand;

import com.JiuShuFangSecondHand.agent.impl.RouterAgent;
import com.JiuShuFangSecondHand.util.UserContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootTest
public class AgentTest {

    @MockitoBean
    private ServerEndpointExporter serverEndpointExporter;

    @Resource
    private RouterAgent routerAgent;

    @Test
    void test() {
        UserContext.setUser(1L);
        String c1 = routerAgent.contentSimple("有没有哈利波特", "123456");
        System.out.println("c1 = " + c1);
        String c2 = routerAgent.contentSimple("我想上架一本书", "123456");
        System.out.println("c2 = " + c2);
    }
}
