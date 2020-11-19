package edu.opjms.candidateSelector.util;

public enum PrefectPosts {
    HOUSE_PREFECT_BOY("house_boy", "<!--House Prefect Boy Section-->"),
    HOUSE_PREFECT_GIRL("house_girl", "<!--House Prefect Girl Section-->"),
    SPORTS_PREFECT_BOY("sports_boy", "<!--Sports Prefect Boy Section-->"),
    SPORTS_PREFECT_GIRL("sports_girl","<!--Sports Prefect Girl Section-->");

    public String cssName;
    public String placeHolderName;

    PrefectPosts(String cssName, String placeHolderName) {
        this.cssName = cssName;
        this.placeHolderName = placeHolderName;
    }
}
