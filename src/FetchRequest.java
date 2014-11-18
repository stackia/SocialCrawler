/**
 * Project: QQSpider
 * Package: PACKAGE_NAME
 * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
 */
public class FetchRequest implements Comparable {
    private static int globalSeq = 0;
    private QQUser user;
    private Type type;
    private FetchWorker.OnRequestFinishedListener onRequestFinishedListener;
    private Priority priority = Priority.Normal;
    private int seq;

    public FetchRequest(QQUser user, Type type) {
        this.user = user;
        this.type = type;
        seq = globalSeq;
        ++globalSeq;
    }

    public FetchRequest(QQUser user, Type type, FetchWorker.OnRequestFinishedListener onRequestFinishedListener) {
        this(user, type);
        this.onRequestFinishedListener = onRequestFinishedListener;
    }

    public FetchWorker.OnRequestFinishedListener getOnRequestFinishedListener() {
        return onRequestFinishedListener;
    }

    public void setOnRequestFinishedListener(FetchWorker.OnRequestFinishedListener onRequestFinishedListener) {
        this.onRequestFinishedListener = onRequestFinishedListener;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof FetchRequest) {
            FetchRequest target = (FetchRequest) o;
            int primary = Integer.compare(priority.getPriorityValue(), target.priority.getPriorityValue());
            if (primary == 0) {
                return Integer.compare(seq, target.seq);
            } else {
                return primary;
            }
        }
        return 0;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public QQUser getUser() {
        return user;
    }

    public Type getType() {
        return type;
    }

    public enum Priority {
        High(0),
        Normal(50),
        Low(100);

        private int priority;

        Priority(int priority) {
            this.priority = priority;
        }

        public int getPriorityValue() {
            return priority;
        }
    }

    public enum Type {
        Friends,
        RecentVisitors,
        MessageBoard,
        FullProfiles,
        SimpleProfiles,
    }
}
