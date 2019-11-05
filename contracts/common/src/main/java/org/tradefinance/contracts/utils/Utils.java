package org.tradefinance.contracts.utils;

import java.lang.reflect.Array;

public class Utils {
    public static String invalidIdentityNameErrorBuilder(String type) {
        return "Invalid " + type + ". Should be of format <NAME>@<EP_ID>";
    }

    public static boolean isIdentityNameValidFormat(String identityName) throws RuntimeException {
        boolean isValid = identityName.split("@").length == 2;
        return isValid;
    }

    public static String getIdentityOrg(String identityName) {
        if (!Utils.isIdentityNameValidFormat(identityName)) {
            throw new RuntimeException("Identity name invalid. Must contain @");
        }

        return identityName.split("@")[1];
    }

    public static String[] getOrgNameListFromIdentities(String ...identityNames) throws RuntimeException {
        String[] orgNames = new String[identityNames.length];
        for (int i = 0; i < identityNames.length; i++) {
            orgNames[i] = Utils.getIdentityOrg(identityNames[i]);
        }
        return orgNames;
    }

    public static <T> T[] append(T[] a, T b) {
        @SuppressWarnings("unchecked")
        T[] bArr = (T[]) Array.newInstance(b.getClass(), 1);
        bArr[0] = b;
        return Utils.concatenate(a, bArr);
    }

    @SafeVarargs
    public static <T> T[] append(T[] a, T... b) {
        return Utils.concatenate(a, b);
    }

    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
