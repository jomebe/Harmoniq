# Harmoniq

Harmoniq는 Audius의 공개 음악 카탈로그와 Android Media3를 사용하는 무료 음악 스트리밍 앱입니다. API 키나 유료 구독 없이 인기곡 탐색, 검색, 자동 다음 곡, 로컬 취향 추천, 백그라운드 재생을 제공합니다.

## 주요 기능

- Audius 인기 음악 및 아티스트·곡·장르 검색
- ExoPlayer 기반 MP3 스트리밍
- MediaSessionService 백그라운드 재생
- 알림·잠금화면 재생/일시정지/이전/다음 컨트롤
- 재생 목록의 다음 곡 자동 재생
- 최근 청취 아티스트·장르 기반 기기 내 추천
- 저장한 곡과 최근 재생 기록을 Room에 보관
- 화면 꺼짐, 앱 전환, 오디오 출력 변경 처리

## 실행

Android Studio에서 프로젝트를 열고 Gradle Sync 후 실행합니다. 별도의 API 키나 OAuth 설정은 필요하지 않습니다.

빌드 명령: ./gradlew testDebugUnitTest lintDebug assembleDebug

## 구조

- data: Audius REST API, Room, 저장소
- domain: 트랙/큐 모델과 로컬 추천 엔진
- player: Media3 MediaSessionService와 앱 컨트롤러
- ui: Compose 화면, 컴포넌트, 테마

## 데이터와 라이선스

음악 메타데이터와 스트림은 Audius API에서 제공됩니다. 각 음원의 권리와 접근 조건은 업로더가 Audius에서 설정한 조건을 따릅니다. Harmoniq는 음원을 다운로드하거나 재배포하지 않으며, 재생 기록은 사용자 기기에만 저장합니다.
