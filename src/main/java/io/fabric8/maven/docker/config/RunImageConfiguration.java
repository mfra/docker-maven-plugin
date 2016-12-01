package io.fabric8.maven.docker.config;

import io.fabric8.maven.docker.util.DeepCopy;
import io.fabric8.maven.docker.util.EnvUtil;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author roland
 * @since 02.09.14
 */
public class RunImageConfiguration implements Serializable {

    static final RunImageConfiguration DEFAULT = new RunImageConfiguration();

    /**
     * Environment variables to set when starting the container. key: variable name, value: env value
     */
    @Parameter
    private Map<String, String> env;

    @Parameter
    private Map<String, String> labels;

    // Path to a property file holding environment variables
    @Parameter
    private String envPropertyFile;

    // Command to execute in container
    @Parameter
    private Arguments cmd;

    // container domain name
    @Parameter
    private String domainname;

    // container domain name
    @Parameter
    private List<String> dependsOn;

    // container entry point
    @Parameter
    private Arguments entrypoint;

    // container hostname
    @Parameter
    private String hostname;

    // container user
    @Parameter
    private String user;

    // working directory
    @Parameter
    private String workingDir;

    // Size of /dev/shm in bytes
    /**
     * @parameter
     */
    private Long shmSize;

    // memory in bytes
    @Parameter
    private Long memory;

    // total memory (swap + ram) in bytes, -1 to disable
    @Parameter
    private Long memorySwap;

    // Path to a file where the dynamically mapped properties are written to
    @Parameter
    private String portPropertyFile;

    // For simple network setups. For complex stuff use "network"
    @Parameter
    private String net;

    @Parameter
    private NetworkConfig network;

    @Parameter
    private List<String> dns;

    @Parameter
    private List<String> dnsSearch;

    @Parameter
    private List<String> capAdd;

    @Parameter
    private List<String> capDrop;

    @Parameter
    private List<String> securityOpts;

    @Parameter
    private Boolean privileged;

    @Parameter
    private List<String> extraHosts;

    // Port mapping. Can contain symbolic names in which case dynamic
    // ports are used
    @Parameter
    private List<String> ports;

    @Parameter
    private NamingStrategy namingStrategy;

    /**
     * Property key part used to expose the container ip when running.
     */
    @Parameter
    private String exposedPropertyKey;

    // Mount volumes from the given image's started containers
    @Parameter
    private VolumeConfiguration volumes;

    // Links to other container started
    @Parameter
    private List<String> links;

    // Configuration for how to wait during startup of the container
    @Parameter
    private WaitConfiguration wait;

    // Mountpath for tmps
    @Parameter
    private List<String> tmpfs;

    @Parameter
    private LogConfiguration log;

    @Parameter
    private RestartPolicy restartPolicy;

    @Parameter
    private List<UlimitConfig> ulimits;

    @Parameter
    private boolean skip = false;

    @Parameter
    private boolean keepEnvs = false;

    public RunImageConfiguration() {
    }

    public String initAndValidate() {
        if (entrypoint != null) {
            entrypoint.validate();
        }
        if (cmd != null) {
            cmd.validate();
        }

        // Custom networks are available since API 1.21 (Docker 1.9)
        final NetworkConfig config = getNetworkingConfig();
        if (config != null && config.isCustomNetwork()) {
            return "1.21";
        }

        return null;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getEnvPropertyFile() {
        return envPropertyFile;
    }

    public Arguments getEntrypoint() {
        return entrypoint;
    }

    public String getHostname() {
        return hostname;
    }

    public String getDomainname() {
        return domainname;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public String getUser() {
        return user;
    }

    public Long getShmSize() {
        return shmSize;
    }

    public Long getMemory() {
        return memory;
    }

    public Long getMemorySwap() {
        return memorySwap;
    }

    public List<String> getPorts() {
        return (ports != null) ? ports : Collections.<String>emptyList();
    }

    public Arguments getCmd() {
        return cmd;
    }

    public String getPortPropertyFile() {
        return portPropertyFile;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public WaitConfiguration getWaitConfiguration() {
        return wait;
    }

    public LogConfiguration getLogConfiguration() {
        return log;
    }

    public List<String> getCapAdd() {
        return capAdd;
    }

    public List<String> getCapDrop() {
        return capDrop;
    }

    public List<String> getSecurityOpts() {
        return securityOpts;
    }

    public List<String> getDns() {
        return dns;
    }

    public NetworkConfig getNetworkingConfig() {
        if (network != null) {
            return network;
        } else if (net != null) {
            return new NetworkConfig(net);
        } else {
            return new NetworkConfig();
        }
    }

    public List<String> getDnsSearch() {
        return dnsSearch;
    }

    public List<String> getExtraHosts() {
        return extraHosts;
    }

    public VolumeConfiguration getVolumeConfiguration() {
        return volumes;
    }

    public List<String> getLinks() {
        return EnvUtil.splitAtCommasAndTrim(links);
    }

    public List<UlimitConfig> getUlimits() {
        return ulimits;
    }

    public List<String> getTmpfs() {
        return tmpfs;
    }

    // Naming scheme for how to name container
    public enum NamingStrategy {
        /**
         * No extra naming
         */
        none,
        /**
         * Use the alias as defined in the configuration
         */
        alias
    }

    public NamingStrategy getNamingStrategy() {
        return namingStrategy == null ? NamingStrategy.none : namingStrategy;
    }

    public String getExposedPropertyKey() {
        return exposedPropertyKey;
    }

    public Boolean getPrivileged() {
        return privileged;
    }

    public RestartPolicy getRestartPolicy() {
        return (restartPolicy == null) ? RestartPolicy.DEFAULT : restartPolicy;
    }

    public boolean skip() {
        return skip;
    }

    public boolean keepEnvs() {
        return keepEnvs;
    }
    // ======================================================================================

    public static class Builder {

        public Builder(final RunImageConfiguration config) {
            if (config == null) {
                this.config = new RunImageConfiguration();
            } else {
                this.config = DeepCopy.copy(config);
            }
        }

        public Builder() {
            this(null);
        }

        private final RunImageConfiguration config;

        public Builder env(final Map<String, String> env) {
            config.env = env;
            return this;
        }

        public Builder labels(final Map<String, String> labels) {
            config.labels = labels;
            return this;
        }


        public Builder envPropertyFile(final String envPropertyFile) {
            config.envPropertyFile = envPropertyFile;
            return this;
        }

        public Builder cmd(final String cmd) {
            if (cmd != null) {
                config.cmd = new Arguments(cmd);
            }
            return this;
        }

        public Builder cmd(final Arguments args) {
            config.cmd = args;
            return this;
        }

        public Builder domainname(final String domainname) {
            config.domainname = domainname;
            return this;
        }

        public Builder entrypoint(final String entrypoint) {
            if (entrypoint != null) {
                config.entrypoint = new Arguments(entrypoint);
            }
            return this;
        }

        public Builder entrypoint(final Arguments args) {
            config.entrypoint = args;
            return this;
        }

        public Builder hostname(final String hostname) {
            config.hostname = hostname;
            return this;
        }

        public Builder portPropertyFile(final String portPropertyFile) {
            config.portPropertyFile = portPropertyFile;
            return this;
        }

        public Builder workingDir(final String workingDir) {
            config.workingDir = workingDir;
            return this;
        }

        public Builder user(final String user) {
            config.user = user;
            return this;
        }

        public Builder shmSize(final Long shmSize) {
            config.shmSize = shmSize;
            return this;
        }

        public Builder memory(final Long memory) {
            config.memory = memory;
            return this;
        }

        public Builder memorySwap(final Long memorySwap) {
            config.memorySwap = memorySwap;
            return this;
        }

        public Builder capAdd(final List<String> capAdd) {
            config.capAdd = capAdd;
            return this;
        }

        public Builder capDrop(final List<String> capDrop) {
            config.capDrop = capDrop;
            return this;
        }

        public Builder securityOpts(final List<String> securityOpts) {
            config.securityOpts = securityOpts;
            return this;
        }

        public Builder net(final String net) {
            config.net = net;
            return this;
        }

        public Builder network(final NetworkConfig networkConfig) {
            config.network = networkConfig;
            return this;
        }

        public Builder dependsOn(final List<String> dependsOn) {
            config.dependsOn = dependsOn;
            return this;
        }

        public Builder dns(final List<String> dns) {
            config.dns = dns;
            return this;
        }

        public Builder dnsSearch(final List<String> dnsSearch) {
            config.dnsSearch = dnsSearch;
            return this;
        }

        public Builder extraHosts(final List<String> extraHosts) {
            config.extraHosts = extraHosts;
            return this;
        }

        public Builder ulimits(final List<UlimitConfig> ulimits) {
            config.ulimits = ulimits;
            return this;
        }

        public Builder ports(final List<String> ports) {
            config.ports = ports;
            return this;
        }

        public Builder volumes(final VolumeConfiguration volumes) {
            config.volumes = volumes;
            return this;
        }

        public Builder links(final List<String> links) {
            config.links = links;
            return this;
        }

        public Builder tmpfs(final List<String> tmpfs) {
            config.tmpfs = tmpfs;
            return this;
        }

        public Builder wait(final WaitConfiguration wait) {
            config.wait = wait;
            return this;
        }

        public Builder log(final LogConfiguration log) {
            config.log = log;
            return this;
        }

        public Builder namingStrategy(final String namingStrategy) {
            config.namingStrategy = namingStrategy == null ?
                    NamingStrategy.none :
                    NamingStrategy.valueOf(namingStrategy.toLowerCase());
            return this;
        }

        public Builder namingStrategy(final NamingStrategy namingStrategy) {
            config.namingStrategy = namingStrategy;
            return this;
        }

        public Builder exposedPropertyKey(final String key) {
            config.exposedPropertyKey = key;
            return this;
        }

        public Builder privileged(final Boolean privileged) {
            config.privileged = privileged;
            return this;
        }

        public Builder restartPolicy(final RestartPolicy restartPolicy) {
            config.restartPolicy = restartPolicy;
            return this;
        }

        public Builder skip(final String skip) {
            if (skip != null) {
                config.skip = Boolean.valueOf(skip);
            }
            return this;
        }

        public Builder keepEnvs(final String keepEnvs) {
            if (keepEnvs != null) {
                config.keepEnvs = Boolean.valueOf(keepEnvs);
            }
            return this;
        }

        public RunImageConfiguration build() {
            return config;
        }
    }
}
