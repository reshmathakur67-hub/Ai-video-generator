package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GenerationMode(val displayName: String) {
    TEXT_TO_VIDEO("Text to Video"),
    IMAGE_TO_VIDEO("Image to Video"),
    SCENE_BUILDER("Scene Builder")
}

enum class AspectRatioOption(val label: String, val ratioString: String, val widthFactor: Float, val heightFactor: Float) {
    PORTRAIT_9_16("9:16", "9:16 (Shorts/Reels)", 9f, 16f),
    LANDSCAPE_16_9("16:9", "16:9 (YouTube/Cinema)", 16f, 9f),
    SQUARE_1_1("1:1", "1:1 (Square)", 1f, 1f)
}

enum class DurationOption(val seconds: Int, val label: String) {
    FIVE_SEC(5, "5s"),
    TEN_SEC(10, "10s"),
    THIRTY_SEC(30, "30s (Multi-clip)")
}

enum class VideoQualityOption(val label: String, val resolution: String) {
    STANDARD("Standard", "720p"),
    HIGH_QUALITY("High Quality", "1080p / 4K")
}

enum class CameraMotionOption(val label: String, val promptModifier: String) {
    AUTO("Auto", "cinematic dynamic camera"),
    STATIC("Static", "locked camera, static angle"),
    ZOOM_IN("Zoom In", "smooth slow zoom in"),
    ZOOM_OUT("Zoom Out", "smooth slow zoom out"),
    PAN_LEFT("Pan Left", "smooth pan left"),
    PAN_RIGHT("Pan Right", "smooth pan right"),
    DOLLY_IN("Dolly In", "dolly in forward motion"),
    DOLLY_OUT("Dolly Out", "dolly out pulling back"),
    ORBIT("Orbit", "360 degree orbit camera rotation"),
    TRACKING_SHOT("Tracking", "smooth tracking shot following subject"),
    CRANE_SHOT("Crane", "high crane jib downward swoop"),
    HANDHELD("Handheld", "organic handheld cinematic movement")
}

enum class MotionStrengthOption(val label: String, val speedFactor: Float) {
    LOW("Low", 0.5f),
    MEDIUM("Medium", 1.0f),
    HIGH("High", 1.8f)
}

enum class VideoStyleOption(val title: String, val promptAddon: String, val iconName: String) {
    REALISTIC("Realistic", "photorealistic 8k documentary style, ultra sharp focus, authentic textures", "camera"),
    CINEMATIC("Cinematic", "epic cinematic movie lighting, 35mm anamorphic lens, shallow depth of field, blockbuster color grading", "movie"),
    THREE_D_CARTOON("3D Cartoon", "Pixar/Dreamworks style 3D animation, vibrant character design, smooth subsurface scattering", "toys"),
    FAMILY_ANIMATION("Family Animation", "warm Disney/Illumination animated storybook style, expressive characters, soft lighting", "palette"),
    ANIME("Anime", "Makoto Shinkai / Studio Ghibli style Japanese anime, detailed cel shading, radiant atmospheric lighting", "brush"),
    FANTASY("Fantasy", "high fantasy concept art, magical glowing particles, ethereal atmosphere, majestic composition", "auto_awesome"),
    CLAY_ANIMATION("Clay Animation", "Aardman stop-motion claymation, tactile clay textures, subtle thumbprints, playful physics", "animation"),
    WATERCOLOR("Watercolor", "delicate flowing watercolor illustration, ink outlines, dreamy pastel bleeding textures", "format_paint"),
    COMIC("Comic", "bold graphic novel art style, dynamic ink lines, half-tone dots, high contrast shading", "menu_book"),
    CYBERPUNK("Cyberpunk", "neon-soaked cyberpunk cityscape, holographic reflections, synthwave glow, chromatic aberration", "lightbulb"),
    INDIAN_CINEMATIC("Indian Cinematic", "grand Bollywood cinematic scale, rich vibrant festive colors, dramatic atmospheric lighting, high drama", "theater_comedy"),
    PRODUCT_AD("Product Ad", "commercial studio lighting, immaculate product showcase, 120fps slow motion, pristine macro details", "shopping_bag"),
    NATURE_DOCS("Nature Docs", "National Geographic nature documentary style, telephoto lens, lifelike wildlife behavior, natural lighting", "nature"),
    CUSTOM("Custom / None", "", "tune")
}

enum class ProviderType(val displayName: String, val description: String) {
    DEVELOPMENT_MOCK("Development Mode", "Simulates async generation jobs for UI testing without spending video credits"),
    GOOGLE_VEO("Google Veo (Preview)", "Direct adapter for Google Veo 3.1 video-generation models"),
    SECURE_BACKEND("Custom Secure Backend", "Production enterprise REST endpoint proxying Runway/Kling/Veo")
}

enum class JobStatus(val displayName: String) {
    PREPARING("Preparing"),
    QUEUED("Queued"),
    GENERATING("Generating"),
    PROCESSING("Processing"),
    ADDING_AUDIO("Adding Audio"),
    FINALIZING("Finalizing"),
    COMPLETED("Completed"),
    FAILED("Failed")
}

data class SceneItem(
    val sceneNumber: Int,
    val durationSeconds: Int = 10,
    val prompt: String = "",
    val characterName: String = "",
    val dialogueText: String = "",
    val voiceType: String = "English Female"
)

data class VoiceConfiguration(
    val enabled: Boolean = false,
    val language: String = "English", // "No Voice", "Hindi Voice", "English Voice"
    val voiceGender: String = "Female", // "Female", "Male"
    val dialogueScript: String = "",
    val speakerName: String = "Narrator",
    val enableLipSync: Boolean = false,
    val backgroundMusic: Boolean = true,
    val soundEffects: Boolean = true
)

data class GenerationSettings(
    val duration: DurationOption = DurationOption.FIVE_SEC,
    val aspectRatio: AspectRatioOption = AspectRatioOption.LANDSCAPE_16_9,
    val style: VideoStyleOption = VideoStyleOption.CINEMATIC,
    val quality: VideoQualityOption = VideoQualityOption.HIGH_QUALITY,
    val cameraMotion: CameraMotionOption = CameraMotionOption.AUTO,
    val motionStrength: MotionStrengthOption = MotionStrengthOption.MEDIUM,
    val negativePrompt: String = "blurry, distorted face, extra fingers, text artifacts, jerky motion, inconsistent clothing",
    val keepCharacterConsistent: Boolean = true,
    val voiceConfig: VoiceConfiguration = VoiceConfiguration(),
    val providerType: ProviderType = ProviderType.DEVELOPMENT_MOCK
)

@Entity(tableName = "video_projects")
data class VideoProject(
    @PrimaryKey val id: String,
    val title: String,
    val mode: GenerationMode,
    val prompt: String,
    val enhancedPrompt: String? = null,
    val imageReferenceUri: String? = null,
    val startingFrameUri: String? = null,
    val endingFrameUri: String? = null,
    val characterReferenceUri: String? = null,
    val durationSeconds: Int,
    val aspectRatio: String,
    val styleName: String,
    val quality: String,
    val cameraMotion: String,
    val motionStrength: String,
    val negativePrompt: String,
    val keepCharacterConsistent: Boolean,
    val voiceLanguage: String?,
    val voiceGender: String?,
    val dialogue: String?,
    val enableLipSync: Boolean,
    val backgroundMusic: Boolean,
    val soundEffects: Boolean,
    val scenesJson: String? = null,
    val status: JobStatus,
    val jobId: String,
    val progressPercentage: Int = 0,
    val statusMessage: String = "",
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val providerType: String,
    val isDevelopmentMode: Boolean = false,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class GenerationJob(
    val jobId: String,
    val projectId: String,
    val status: JobStatus,
    val progressPercentage: Int = 0,
    val statusMessage: String = "",
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val errorMessage: String? = null,
    val isMock: Boolean = false
)
