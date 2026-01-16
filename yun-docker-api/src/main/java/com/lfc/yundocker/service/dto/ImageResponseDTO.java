package com.lfc.yundocker.service.dto;

import com.github.dockerjava.api.command.InspectImageResponse;
import java.io.Serializable;

public class ImageResponseDTO implements Serializable {
    private String id;
    private String[] repoTags;
    private String created;
    private Long size;
    private String architecture;
    private Integer port;

    public ImageResponseDTO() {
    }

    public ImageResponseDTO(InspectImageResponse response) {
        this.id = response.getId();
        this.repoTags = response.getRepoTags().toArray(new String[0]);
        this.created = response.getCreated();
        this.size = response.getSize();
        this.architecture = response.getArch();
        if (response.getConfig() != null && response.getConfig().getExposedPorts() != null
                && response.getConfig().getExposedPorts().length > 0) {
            this.port = response.getConfig().getExposedPorts()[0].getPort();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getRepoTags() {
        return repoTags;
    }

    public void setRepoTags(String[] repoTags) {
        this.repoTags = repoTags;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}