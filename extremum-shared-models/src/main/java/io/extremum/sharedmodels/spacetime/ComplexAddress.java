package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.basic.Multilingual;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComplexAddress {
    private Type type;
    private String string;
    private Multilingual multilingual;
    private Address address;

    public ComplexAddress() {
        type = Type.unknown;
    }

    public ComplexAddress(String stringAddress) {
        type = Type.string;
        string = stringAddress;
    }

    public ComplexAddress(Multilingual multilingualAddress) {
        type = Type.multilingual;
        multilingual = multilingualAddress;
    }

    public ComplexAddress(Address objectAddress) {
        type = Type.addressObject;
        address = objectAddress;
    }

    public enum Type {
        unknown, string, multilingual, addressObject
    }
}
