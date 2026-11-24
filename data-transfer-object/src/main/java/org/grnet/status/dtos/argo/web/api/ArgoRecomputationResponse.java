package org.grnet.status.dtos.argo.web.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArgoRecomputationResponse<T> {

   // private ArgoWebApiStatusDto status;

    private T data;
}
