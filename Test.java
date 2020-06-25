死锁
public class Test {
    public static void main(String[] args) throws Exception {
        Object A=new Object();
        Object B=new Object();
        new Thread(()->{
            synchronized(A){
                System.out.println("我有A资源，我想获取B资源");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized(B){
                    System.out.println("我有A资源，我已经获取B资源");
                }
            }
        }).start();

        new Thread(()->{
            synchronized(B){
                System.out.println("我有B资源，我想获取A资源");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized(A){
                    System.out.println("我有B资源，我已经获取A资源");
                }
            }
        }).start();
    }
}

countDownLatch:

public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch=new CountDownLatch(20);
        new Thread(()->{
            System.out.println("开始考试");
            try {
                countDownLatch.await();
                System.out.println("收工");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        for(int i=0;i<20;i++){
            final int j=i;
            new Thread(()->{
                System.out.println(j+"ok");
                countDownLatch.countDown();
            }).start();
        }
    }
	
	
cyclicbarrier:
class Singler implements Runnable{

    @Override
    public void run() {
        System.out.println("唱歌咯");
    }
}
public class Test {
    public static void main(String[] args) throws Exception {
        CyclicBarrier cyclicBarrier=new CyclicBarrier(3,new Singler());
        for(int i=0;i<3;i++){
            final int j=i;
            new Thread(()->{
                System.out.println(j+"正在爬山");
                try {
                    cyclicBarrier.await();
                    System.out.println(j+"爬上山了");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}



Exchanger:

class Tool {
    String name;
    String work;
    public Tool(String name,String work){
        this.name=name;
        this.work=work;
    }
}
class MyThread implements Runnable{
    String name;
    Tool tool;
    Exchanger<Tool> e;
    public MyThread(String name,Tool tool,Exchanger<Tool> e){
        this.name=name;
        this.tool=tool;
        this.e=e;
    }
    @Override
    public void run() {
        System.out.println(name+"正在用"+tool.name+tool.work);
        try {
            System.out.println("交换工具");
            Thread.sleep(1000);
            tool=e.exchange(tool);
            System.out.println(name+"正在用"+tool.name+tool.work);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}
public class Test {
    public static void main(String[] args) throws Exception {
        Exchanger<Tool> e=new Exchanger<>();
        new Thread
                (new MyThread("小明",new Tool("拖把","扫地"),e)).start();
        new Thread
                (new MyThread("小红",new Tool("抹布","擦卓"),e)).start();
    }
}


生产者消费者
class Productor implements Runnable{
    MyQueue queue;
    public Productor(MyQueue queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            try {
                Thread.sleep(100);
                queue.put(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

class Consumer implements Runnable{
    MyQueue queue;
    public Consumer(MyQueue queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            try {
                Thread.sleep(100);
                queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MyQueue{
    Lock lock=new ReentrantLock();
    Condition pro=lock.newCondition();
    Condition con=lock.newCondition();

    Object[] arr=new Object[100];
    int capacity=100;
    int putIndex;
    int takeIndex;
    int count;
    public void put(Object o){
        try{
            lock.lock();
            while(count==capacity){
                System.out.println("已经满了,目前为"+count);
                pro.await();
            }
            arr[putIndex++]=o;
            System.out.println("生产者生成"+o);
            if(putIndex>=capacity){
                putIndex=0;
            }
            count++;
            con.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void take(){
        try{
            lock.lock();
            while(0==count){
                System.out.println("现在为空"+count);
                con.await();
            }
            Object ret=arr[takeIndex++];
            System.out.println("消费者消费"+ret);
            if(takeIndex>=capacity){
                takeIndex=0;
            }
            count--;
            con.signal();
        }catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            lock.unlock();
        }
    }
}
public class Test {
    public static void main(String[] args) throws Exception {
        MyQueue queue=new MyQueue();
        new Thread(new Productor(queue)).start();
        new Thread(new Productor(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}