package com.simulator.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.Network;
import com.simulator.model.ContainerStatus;
import com.simulator.model.SimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DockerService {

    private static final Logger logger = LoggerFactory.getLogger(DockerService.class);

    private final DockerClient dockerClient;
    
    @Value("${docker.container-name:simulator-mysql}")
    private String containerName;
    
    @Value("${docker.network-name:simulator-network}")
    private String networkName;

    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public ContainerStatus getContainerStatus() {
        ContainerStatus status = new ContainerStatus();
        status.setContainerName(containerName);
        
        try {
            Container container = findContainer();
            if (container != null) {
                status.setContainerId(container.getId());
                status.setState(container.getState());
                status.setStatus(container.getStatus());
                status.setRunning("running".equalsIgnoreCase(container.getState()));
                
                // Check if paused
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
                status.setPaused(Boolean.TRUE.equals(inspect.getState().getPaused()));
                
                // Check network connection
                NetworkSettings networkSettings = inspect.getNetworkSettings();
                if (networkSettings != null && networkSettings.getNetworks() != null) {
                    status.setNetworkConnected(networkSettings.getNetworks().containsKey(networkName));
                    if (status.isNetworkConnected()) {
                        status.setNetworkId(networkSettings.getNetworks().get(networkName).getNetworkID());
                    }
                }
            } else {
                status.setState("not_found");
                status.setStatus("Container not found");
            }
        } catch (Exception e) {
            logger.error("Error getting container status", e);
            status.setState("error");
            status.setStatus("Error: " + e.getMessage());
        }
        
        return status;
    }

    public SimulationResult disconnectNetwork() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("NETWORK_DISCONNECT", "Container not found: " + containerName);
            }
            
            Network network = findNetwork();
            if (network == null) {
                return SimulationResult.failure("NETWORK_DISCONNECT", "Network not found: " + networkName);
            }
            
            dockerClient.disconnectFromNetworkCmd()
                    .withContainerId(container.getId())
                    .withNetworkId(network.getId())
                    .withForce(true)
                    .exec();
            
            logger.info("Disconnected container {} from network {}", containerName, networkName);
            return SimulationResult.success("NETWORK_DISCONNECT", "Container disconnected from network");
            
        } catch (Exception e) {
            logger.error("Failed to disconnect network", e);
            return SimulationResult.failure("NETWORK_DISCONNECT", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult reconnectNetwork() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("NETWORK_RECONNECT", "Container not found: " + containerName);
            }
            
            Network network = findNetwork();
            if (network == null) {
                return SimulationResult.failure("NETWORK_RECONNECT", "Network not found: " + networkName);
            }
            
            dockerClient.connectToNetworkCmd()
                    .withContainerId(container.getId())
                    .withNetworkId(network.getId())
                    .exec();
            
            logger.info("Reconnected container {} to network {}", containerName, networkName);
            return SimulationResult.success("NETWORK_RECONNECT", "Container reconnected to network");
            
        } catch (Exception e) {
            logger.error("Failed to reconnect network", e);
            return SimulationResult.failure("NETWORK_RECONNECT", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult stopContainer() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("CONTAINER_STOP", "Container not found: " + containerName);
            }
            
            dockerClient.stopContainerCmd(container.getId())
                    .withTimeout(10)
                    .exec();
            
            logger.info("Stopped container {}", containerName);
            return SimulationResult.success("CONTAINER_STOP", "Container stopped (simulating DB crash)");
            
        } catch (Exception e) {
            logger.error("Failed to stop container", e);
            return SimulationResult.failure("CONTAINER_STOP", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult startContainer() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("CONTAINER_START", "Container not found: " + containerName);
            }
            
            dockerClient.startContainerCmd(container.getId()).exec();
            
            logger.info("Started container {}", containerName);
            return SimulationResult.success("CONTAINER_START", "Container started (DB recovering)");
            
        } catch (Exception e) {
            logger.error("Failed to start container", e);
            return SimulationResult.failure("CONTAINER_START", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult pauseContainer() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("CONTAINER_PAUSE", "Container not found: " + containerName);
            }
            
            dockerClient.pauseContainerCmd(container.getId()).exec();
            
            logger.info("Paused container {} (simulating connection timeout)", containerName);
            return SimulationResult.success("CONTAINER_PAUSE", "Container paused (simulating timeout/hang)");
            
        } catch (Exception e) {
            logger.error("Failed to pause container", e);
            return SimulationResult.failure("CONTAINER_PAUSE", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult unpauseContainer() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("CONTAINER_UNPAUSE", "Container not found: " + containerName);
            }
            
            dockerClient.unpauseContainerCmd(container.getId()).exec();
            
            logger.info("Unpaused container {}", containerName);
            return SimulationResult.success("CONTAINER_UNPAUSE", "Container unpaused (timeout cleared)");
            
        } catch (Exception e) {
            logger.error("Failed to unpause container", e);
            return SimulationResult.failure("CONTAINER_UNPAUSE", "Failed: " + e.getMessage());
        }
    }

    public SimulationResult restartContainer() {
        try {
            Container container = findContainer();
            if (container == null) {
                return SimulationResult.failure("CONTAINER_RESTART", "Container not found: " + containerName);
            }
            
            dockerClient.restartContainerCmd(container.getId())
                    .withTimeout(30)
                    .exec();
            
            logger.info("Restarted container {}", containerName);
            return SimulationResult.success("CONTAINER_RESTART", "Container restarted (DB service restart)");
            
        } catch (Exception e) {
            logger.error("Failed to restart container", e);
            return SimulationResult.failure("CONTAINER_RESTART", "Failed: " + e.getMessage());
        }
    }

    private Container findContainer() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName))
                .exec();
        
        return containers.stream()
                .filter(c -> {
                    for (String name : c.getNames()) {
                        if (name.equals("/" + containerName) || name.equals(containerName)) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
    }

    private Network findNetwork() {
        List<Network> networks = dockerClient.listNetworksCmd()
                .withNameFilter(networkName)
                .exec();
        
        return networks.stream()
                .filter(n -> n.getName().equals(networkName))
                .findFirst()
                .orElse(null);
    }

    public boolean isDockerAvailable() {
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            logger.warn("Docker is not available: {}", e.getMessage());
            return false;
        }
    }
}
