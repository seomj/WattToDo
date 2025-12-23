package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.favorite.FavoriteStationDetailDto;
import com.ssafy.wtd.backend.model.FavoriteStation;
import com.ssafy.wtd.backend.repository.FavoriteStationRepository;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteStationRepository favoriteStationRepository;
    private final UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<?> getMyFavorites(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");

        List<FavoriteStationDetailDto> favorites = favoriteStationRepository.findDetailsByUserId(user.getUserId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{stationId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable String stationId, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");

        FavoriteStation existing = favoriteStationRepository.findByUserAndStation(user.getUserId(), stationId);
        Map<String, Object> response = new HashMap<>();

        if (existing != null) {
            favoriteStationRepository.delete(user.getUserId(), stationId);
            response.put("isFavorite", false);
            response.put("message", "즐겨찾기에서 삭제되었습니다.");
        } else {
            FavoriteStation favorite = FavoriteStation.builder()
                    .userId(user.getUserId())
                    .stationId(stationId)
                    .build();
            favoriteStationRepository.insert(favorite);
            response.put("isFavorite", true);
            response.put("message", "즐겨찾기에 추가되었습니다.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{stationId}/check")
    public ResponseEntity<?> checkFavorite(@PathVariable String stationId, Authentication authentication) {
        if (authentication == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");

        FavoriteStation existing = favoriteStationRepository.findByUserAndStation(user.getUserId(), stationId);
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorite", existing != null);

        return ResponseEntity.ok(response);
    }
}
