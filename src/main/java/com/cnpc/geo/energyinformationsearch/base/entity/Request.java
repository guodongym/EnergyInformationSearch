package com.cnpc.geo.energyinformationsearch.base.entity;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Object contains url to crawl.<br>
 * It contains some additional information.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 2062192774891352043L;

    public static final String CYCLE_TRIED_TIMES = "_cycle_tried_times";
    public static final String STATUS_CODE = "statusCode";
    public static final String PROXY = "proxy";
    /**
     * @Field taskId
     * @Description 抓取任务ID
     * @author quwu
     * @Date 2015-4-10
     */
    private String taskId = "common_task";
    /**
     * @Field taskType
     * @Description 抓取任务类型
     * @author quwu
     * @Date 2015-4-21
     */
    private Integer taskType = 1;
    /**
     * @Field series
     * @Description 抓取级数
     * @author quwu
     * @Date 2015-4-21
     */
    private Integer series = 0;
    /**
     * 数据源类型
     */
    private int dataSourceType=1;
    /**
     * 任务名称
     */
    private String dataSourceName="common_name";
    /**
     * @Field maxSeries
     * @Description 最大抓取层数
     * @author quwu
     * @Date 2015-8-19
     */
    private Integer maxSeries = 3;
    /**
     * @Field selfDomin
     * @Description 是否只爬本域名
     * @author quwu
     * @Date 2015-8-19
     */
    private boolean selfDomin = false;
    /**
     * 数据源类别 
     */
    private String sourceFormatType ;
    
    private String url;

    private String method;
    
	private int subTaskType = 0;

    /**
     * Store additional information in extras.
     */
    private Map<String, Object> extras;

    /**
     * Priority of the request.<br>
     * The bigger will be processed earlier. <br>
     * @see us.codecraft.webmagic.scheduler.PriorityScheduler
     */
    private long priority;
    
    /**
     * add by quwu
     */
    //修改次数
    private long x = 0;
    //累积时间
    private long t = 0;
    //误差倍数
    private  int n = 2;
    //更新周期
    private double up;
    //抓取周期
    private double fp;
    //上次抓取时间
    private long last = 0;
    //下次抓取时间
    private long next = 0;
    //已入队并抓取过
    private Boolean alreadyFetch = false;
    //网页md5值
    private String md5 = "";

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public Integer getSeries() {
		return series;
	}

	public void setSeries(Integer series) {
		this.series = series;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public Boolean getAlreadyFetch() {
		return alreadyFetch;
	}

	public void setAlreadyFetch(Boolean alreadyFetch) {
		this.alreadyFetch = alreadyFetch;
	}

	public long getX() {
		return x;
	}

	public void setX(long x) {
		this.x = x;
	}

	public long getT() {
		return t;
	}

	public void setT(long t) {
		this.t = t;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public double getUp() {
		return up;
	}

	public void setUp(double up) {
		this.up = up;
	}

	public double getFp() {
		return fp;
	}

	public void setFp(double fp) {
		this.fp = fp;
	}

	public long getLast() {
		return last;
	}

	public void setLast(long last) {
		this.last = last;
	}

	public long getNext() {
		return next;
	}

	public void setNext(long next) {
		this.next = next;
	}

	public Request() {
    }

    public Request(String url) {
        this.url = url;
    }
    
    public int getSubTaskType() {
		return subTaskType;
	}

	public Request setSubTaskType(int subTaskType) {
		this.subTaskType = subTaskType;
		return this;
	}

    public long getPriority() {
        return priority;
    }

    /**
     * Set the priority of request for sorting.<br>
     * Need a scheduler supporting priority.<br>
     * @see us.codecraft.webmagic.scheduler.PriorityScheduler
     *
     * @param priority
     * @return this
     */
    public Request setPriority(long priority) {
        this.priority = priority;
        return this;
    }

    public Object getExtra(String key) {
        if (extras == null) {
            return null;
        }
        return extras.get(key);
    }

    public Request putExtra(String key, Object value) {
        if (extras == null) {
            extras = new HashMap<String, Object>();
        }
        extras.put(key, value);
        return this;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (!url.equals(request.url)) return false;

        return true;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The http method of the request. Get for default.
     * @return httpMethod
     * @see us.codecraft.webmagic.utils.HttpConstant.Method
     * @since 0.5.0
     */
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(int dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public Integer getMaxSeries() {
		return maxSeries;
	}

	public void setMaxSeries(Integer maxSeries) {
		this.maxSeries = maxSeries;
	}

	public boolean isSelfDomin() {
		return selfDomin;
	}

	public void setSelfDomin(boolean selfDomin) {
		this.selfDomin = selfDomin;
	}


	public String getSourceFormatType() {
		return sourceFormatType;
	}

	public void setSourceFormatType(String sourceFormatType) {
		this.sourceFormatType = sourceFormatType;
	}

	@Override
    public String toString() {
        return "Request{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", extras=" + extras +
                ", priority=" + priority +
                '}';
    }
}
