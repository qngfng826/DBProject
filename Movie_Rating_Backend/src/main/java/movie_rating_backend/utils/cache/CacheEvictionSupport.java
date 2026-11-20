package movie_rating_backend.utils.cache;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 写缓存失效必须发生在事务提交之后，否则并发读可能在提交前把旧值重新回填。
 * 无事务时立即执行（评论写路径没有事务）。
 */
public final class CacheEvictionSupport {

    private CacheEvictionSupport() {
    }

    public static void afterCommit(Runnable action) {
        if (action == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}