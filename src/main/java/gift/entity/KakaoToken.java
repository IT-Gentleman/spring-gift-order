package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "kakao_token")
public class KakaoToken {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Member member;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String accessToken; // encrypted

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String refreshToken; // encrypted

    protected KakaoToken() {
    }

    public KakaoToken(Member member, String accessToken, String refreshToken) {
        this.member = member;
        this.id = member.getId();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
        this.id = member.getId();
    }

}
