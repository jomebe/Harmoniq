# Harmoniq

Harmoniq는 YouTube Data API와 공식 YouTube IFrame Player를 사용하는 Android 음악 탐색 앱입니다. 검색, Google 로그인, 구독/좋아요 기반 초기 추천, 앱 내부 재생 기록 기반 개인화, 자동 재생 큐, 로컬 라이브러리를 제공합니다.

## 포함된 기능

- YouTube 음악 카테고리 검색 및 한국 인기 음악
- Google OAuth 로그인 (`youtube.readonly` 최소 권한)
- 구독 채널과 좋아요 표시한 영상 기반 추천 시드
- 앱 내부 재생 기록·태그·아티스트 선호도를 활용한 로컬 추천 랭킹
- 공식 YouTube IFrame Player 재생
- 곡 종료 감지 후 다음 곡 자동 재생
- 큐가 끝나면 현재 곡과 관련된 음악을 검색해 자동 확장
- 저장한 곡과 최근 재생 기록을 Room에 보관
- API 토큰 로그 마스킹 및 앱 데이터 클라우드 백업 제외

## 의도적으로 포함하지 않은 기능

- YouTube 시청 기록 또는 YouTube 홈 추천 피드 접근
- 앱이 보이지 않거나 화면이 꺼진 상태의 YouTube 재생
- 광고 차단, 음원 분리, 다운로드 및 오프라인 저장

## 실행 준비

1. Android Studio에서 프로젝트를 엽니다.
2. Google Cloud Console에서 프로젝트를 만들고 **YouTube Data API v3**를 활성화합니다.
3. API 키를 생성하고 Android 앱 제한을 설정합니다.
4. OAuth 동의 화면을 구성한 뒤 Android OAuth 클라이언트를 만듭니다.
   - 패키지명: `com.jomebe.harmoniq`
   - 개발/배포 서명 인증서의 SHA-1을 각각 등록해야 합니다.
5. `local.properties.example`을 `local.properties`로 복사하고 값을 입력합니다.

```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
YOUTUBE_API_KEY=YOUR_KEY
```

6. Android Studio에서 Gradle Sync 후 `app`을 실행합니다.

Google 로그인은 OAuth Android Client의 패키지명과 실제 서명 SHA-1이 일치해야 동작합니다. API 키나 서명 파일은 커밋하지 마세요.

## 구조

```text
app/src/main/java/com/jomebe/harmoniq/
├── auth/       Google OAuth 및 YouTube 읽기 권한
├── data/       Room, Retrofit, 저장소
├── domain/     트랙/큐 모델과 추천 엔진
├── player/     공식 YouTube IFrame Player WebView
└── ui/         Compose 화면, 컴포넌트, 테마
```

## 추천 방식

추천 점수는 최근 재생의 시간 감쇠, 좋아요 아티스트, 구독 채널, 태그 일치도, 새 곡 가중치로 계산됩니다. 모든 재생 기록은 기기 내부 Room 데이터베이스에만 저장되며 프로필 화면에서 초기화할 수 있습니다.

## 테스트

```bash
./gradlew testDebugUnitTest
```

## 정책 및 개인정보

이 프로젝트는 YouTube의 공식 Data API와 IFrame Player만 사용합니다. 실제 배포 전에는 Google OAuth 검증, 개인정보처리방침 공개, YouTube API Services 이용약관 및 브랜딩 요구사항 검토가 필요합니다.
