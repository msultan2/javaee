/*
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
 * Java version: JDK 1.8
 *
 * Created By: Liban Abdulkadir
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2.misc;

import static fj.data.Array.array;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ssl.bluetruth.receiver.v2.misc.annotations.Hex;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;

import fj.Effect;
import fj.F;

/**
 *
 * @author Liban Abdulkadir
 */
@Component
@Scope("prototype")
public class RequestParser<T> {

	private Class<T> c;
    private String data;
    private String ip;
    @Autowired
    private ApplicationContext applicationContext;
    private static final Logger logger = LogManager.getRootLogger();

    public RequestParser(Class<T> c) {
    	this.c = c;
    }

    public RequestParser<T> data(String data) {
        this.data = data;
        return this;
    }

    public RequestParser<T> request(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String l = null;
        ip = request.getRemoteAddr();

        try {
            BufferedReader br = request.getReader();
            while ((l = br.readLine()) != null) {
                sb.append(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.data = sb.toString();
        
        logger.info(request.getServletPath() + " - " + data);
        return this;
    }

    private boolean fieldExists(String name) {
        Field[] fields = c.getFields();
        for (Field f : fields) {
            if (f.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public T parse() {
        try {
        	String beanName = Character.toLowerCase(c.getSimpleName().charAt(0)) + c.getSimpleName().substring(1);
            final T nc = applicationContext.getBean(beanName, c);

            Field dataf = c.getField("data");
            dataf.set(nc, this.data);

            final String[] parts = data.split(",");
            int rnd = Integer.valueOf(parts[parts.length - 1], 16);
            Field rndf = c.getField("rnd");
            rndf.set(nc, rnd);

            if (fieldExists("ip")) {
                c.getField("rnd").set(nc, ip);
            }

            /* Parse annotated fields */
            for (Field f : c.getFields()) {
                if (f.isAnnotationPresent(Variable.class)) {
                    Variable v = f.getAnnotation(Variable.class);
                    if (f.getType().equals(int.class)) {
                        if (f.isAnnotationPresent(Hex.class)) {
                            f.set(nc, Integer.valueOf(parts[v.index()], 16));
                        } else {
                            f.set(nc, Integer.parseInt(parts[v.index()]));
                        }
                    } else if (f.getType().equals(String.class)) {
                        f.set(nc, parts[v.index()]);
                    } else if (f.getType().equals(Date.class)) {
                        f.set(nc, new Date(Long.valueOf(parts[v.index()], 16) * 1000L));
                    }
                }
            }

            /* Parse variable=value comma separated pieces */
            array(parts).filter(new F<String, Boolean>() {
                @Override
                public Boolean f(String a) {
                    String[] s = a.split("=");
                    return s.length == 2 && !a.equals(parts[parts.length - 1]);
                }
            }).foreach(new Effect<String>() {
                @Override
                public void e(String s) {
                    String[] ss = s.split("=");
                    try {
                        c.getMethod("varSet", new Class[]{String.class, String.class})
                                .invoke(nc, new Object[]{ss[0], ss[1]});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            c.getMethod("postParse", new Class[0]).invoke(nc, new Object[0]);
            return c.cast(nc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
