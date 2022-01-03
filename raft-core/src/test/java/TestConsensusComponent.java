import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import wheel.core.rpc.message.RequestVoteResult;
import wheel.core.rpc.message.RequestVoteRpc;

import java.net.Socket;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description pub-sub方案解决耦合问题（发布-订阅）
 * 可以使用Guava的Eventbus来弱化依赖关系
 */
public class TestConsensusComponent {

    // 注册
    public void init(EventBus eventBus){
        eventBus.register(this);
    }

    // 订阅request vote rpc消息
    @Subscribe
    public void onRequestVoteRpc(RequestVoteRpc rpc){}

    // 订阅request vote result消息
    @Subscribe
    public void onRequestVoteResult(RequestVoteResult result){}

}

class TestRpcComponent{
    private final EventBus eventBus;

    public TestRpcComponent(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // 处理消息
    public void process(Socket socket){
/*        Request request = parseRequest(socket);
        if(request instanceof RequestVoteResult){
            eventBus.post(request);
        }

        if(request instanceof RequestVoteRpc){
            eventBus.post(request);
        }*/

    }
}
