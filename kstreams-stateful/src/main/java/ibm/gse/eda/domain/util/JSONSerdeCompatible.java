package ibm.gse.eda.domain.util;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ibm.gse.eda.domain.*;


@SuppressWarnings("DefaultAnnotationParam")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
@JsonSubTypes({
                 @JsonSubTypes.Type(value = Purchase.class, name = "purchase")
             })
public interface JSONSerdeCompatible {

}
