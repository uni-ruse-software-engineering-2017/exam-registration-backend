package uniruse.mse.examregistration.security;

public class SecurityConstants {
    public static final int EXPIRATION_TIME = 3600 * 1000;
    public static final String HEADER_STRING = "Authorization";
    public static final String ISSUER = "exams.ami.uni-ruse.bg";
    public static final String SECRET = "secret";
    public static final String TOKEN_PREFIX = "Bearer ";
}