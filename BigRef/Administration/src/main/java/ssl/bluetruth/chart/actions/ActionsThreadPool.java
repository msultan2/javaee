/*
 * ActionsThreadPool.java
 * 
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7
 *
 * Created on 17-Jun-2015 12:10 PM
 * 
 */
package ssl.bluetruth.chart.actions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author svenkataramanappa
 */
public class ActionsThreadPool {
    // the size of the thread pool to use
    private static final int POOL_SIZE = 40;
    // the singleton intance of this class
    private static ActionsThreadPool instance;
    // the thread pool
    private static ScheduledExecutorService pool;
        
    private ActionsThreadPool() {
    }
    
    public static ActionsThreadPool getInstance() {
        synchronized (ActionsThreadPool.class) {
            if (null == instance) {
                instance = new ActionsThreadPool();
            }
        }
        return instance;
    }
    
    public ScheduledExecutorService getPool() {
        if (null == pool) {
            pool = Executors.newScheduledThreadPool(POOL_SIZE, new ThreadFactory() {
                int threadsCreated = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread newThread = Executors.defaultThreadFactory().newThread(r);
                    newThread.setName("Chart Thread-" + threadsCreated++);
                    return newThread;
                }
            });
        }
        return pool;
    }
    
    public static void shutdown() {
        if (null != pool) {
            pool.shutdown();
        }
    }
}
