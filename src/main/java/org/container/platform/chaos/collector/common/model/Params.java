package org.container.platform.chaos.collector.common.model;

import lombok.Data;
import org.container.platform.chaos.collector.common.Constants;
import java.util.List;
import java.util.Map;

/**
 * Params 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Data
public class Params {

    public String cluster = Constants.EMPTY_STRING;
    public String namespace = Constants.EMPTY_STRING;
    public String resource;
    public String resourceName = Constants.EMPTY_STRING;
    public String metadataName =  Constants.EMPTY_STRING;
    public int offset = 0;
    public int limit = 0;
    public String orderBy = "creationTime";
    public String order = "desc";
    public String searchName = Constants.EMPTY_STRING;
    public String ownerReferencesUid = Constants.EMPTY_STRING;
    public String ownerReferencesName = Constants.EMPTY_STRING;
    public String selector = Constants.EMPTY_STRING;
    public String type = Constants.EMPTY_STRING;
    public long id = 0;
    public String userId = Constants.EMPTY_STRING;
    public String userAuthId = Constants.EMPTY_STRING;
    public String userType = Constants.EMPTY_STRING;
    public String isActive = "true";
    public String nodeName = Constants.EMPTY_STRING;
    public String resourceUid = Constants.EMPTY_STRING;
    public String clusterName = Constants.EMPTY_STRING;
    public Integer topN = 5;
    public String saToken = Constants.EMPTY_STRING;
    public String saSecret =  Constants.EMPTY_STRING;
    public Boolean includeUsage = false;

    // request parameter setting
    public String selectorType = Constants.EMPTY_STRING;

    //resource yaml
    public String rs_sa = Constants.EMPTY_STRING;
    public String rs_role = Constants.EMPTY_STRING;
    public String rs_rq = Constants.EMPTY_STRING;
    public String rs_lr = Constants.EMPTY_STRING;

    //provider
    public Object providerInfo = null;
    public String region = Constants.EMPTY_STRING;
    public String project = Constants.EMPTY_STRING;
    public String site = Constants.EMPTY_STRING;

    //hcl
    public String hclScript = Constants.EMPTY_STRING;

    //ssh key
    public String sshKey = Constants.EMPTY_STRING;
    public String privateKey = Constants.EMPTY_STRING;

    //cluster
    public Boolean isClusterRegister = false;
    public String clusterApiUrl = Constants.EMPTY_STRING;
    public String clusterToken = Constants.EMPTY_STRING;
    public String cloudAccountId = Constants.EMPTY_STRING;
    public String clusterType = Constants.CLUSTER_TYPE_SUB;
    public String description = Constants.EMPTY_STRING;
    public String clusterStatus = Constants.ClusterStatus.DISABLED.getInitial();

    // sign Up
    public Boolean isSuperAdmin = false;


    // rest send type
    public Boolean isClusterToken = false;

    // chaos
    public String kind = Constants.EMPTY_STRING;
    public String name = Constants.EMPTY_STRING;
    private String uid = Constants.EMPTY_STRING;
    public String chaosNamespace = Constants.EMPTY_STRING;
    private String duration = Constants.EMPTY_STRING;
    private String action = Constants.EMPTY_STRING;
    private String gracePeriod = Constants.EMPTY_STRING;
    private Map<String, String> labelSelectors = null;
    public List namespaces;
    private Map<String, List<String>> pods = null;
    private String latency = Constants.EMPTY_STRING;
    private Object stressors = null;
    private String object_id = Constants.EMPTY_STRING;
    private Boolean event = false;
    private List statusList;
    private List<Long> stressChaosResourceIds;


    private String browser = Constants.EMPTY_STRING;
    private String clientIp = Constants.EMPTY_STRING;


    private Boolean isGlobal = false;

    public Params(){
        this.cluster = Constants.EMPTY_STRING;
        this.namespace = Constants.EMPTY_STRING;
        this.resource = Constants.EMPTY_STRING;
        this.resourceName = Constants.EMPTY_STRING;
        this.metadataName = Constants.EMPTY_STRING;
        this.offset = 0;
        this.limit = 0;
        this.orderBy = "creationTime";
        this.order = "desc";
        this.searchName = Constants.EMPTY_STRING;
        this.ownerReferencesUid = Constants.EMPTY_STRING;
        this.ownerReferencesName = Constants.EMPTY_STRING;
        this.selector = Constants.EMPTY_STRING;
        this.type = Constants.EMPTY_STRING;
        this.userId = Constants.EMPTY_STRING;
        this.userAuthId = Constants.EMPTY_STRING;
        this.userType = Constants.EMPTY_STRING;
        this.isActive = "true";
        this.nodeName = Constants.EMPTY_STRING;
        this.resourceUid = Constants.EMPTY_STRING;
        this.selectorType = Constants.RESOURCE_NAMESPACE;
        this.rs_sa = Constants.EMPTY_STRING;
        this.rs_role = Constants.EMPTY_STRING;
        this.rs_rq = Constants.EMPTY_STRING;
        this.rs_lr = Constants.EMPTY_STRING;
        this.isSuperAdmin = false;
    }
}