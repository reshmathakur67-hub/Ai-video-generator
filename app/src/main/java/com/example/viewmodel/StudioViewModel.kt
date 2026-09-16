package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.model.AspectRatioOption
import com.example.model.CameraMotionOption
import com.example.model.DurationOption
import com.example.model.GenerationMode
import com.example.model.GenerationSettings
import com.example.model.MotionStrengthOption
import com.example.model.ProviderType
import com.example.model.SceneItem
import com.example.model.VideoProject
import com.example.model.VideoQualityOption
import com.example.model.VideoStyleOption
import com.example.model.VoiceConfiguration
import com.example.service.PromptEnhancerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab {
    HOME,
    CREATE,
    PROJECTS,
    SETTINGS
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = ProjectRepository(database.projectDao(), viewModelScope)
    private val enhancerService = PromptEnhancerService()

    val allProjects: StateFlow<List<VideoProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProjects: StateFlow<List<VideoProject>> = repository.activeProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation State
    private val _currentNavTab = MutableStateFlow(AppNavTab.HOME)
    val currentNavTab: StateFlow<AppNavTab> = _currentNavTab.asStateFlow()

    private val _currentCreateMode = MutableStateFlow(GenerationMode.TEXT_TO_VIDEO)
    val currentCreateMode: StateFlow<GenerationMode> = _currentCreateMode.asStateFlow()

    // Creation Input State
    private val _promptText = MutableStateFlow("A cute 3D cartoon elephant walking through a colorful Indian village, cinematic lighting")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _enhancedPrompt = MutableStateFlow<String?>(null)
    val enhancedPrompt: StateFlow<String?> = _enhancedPrompt.asStateFlow()

    private val _isEnhancingPrompt = MutableStateFlow(false)
    val isEnhancingPrompt: StateFlow<Boolean> = _isEnhancingPrompt.asStateFlow()

    // Reference Images
    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri.asStateFlow()

    private val _characterReferenceUri = MutableStateFlow<String?>(null)
    val characterReferenceUri: StateFlow<String?> = _characterReferenceUri.asStateFlow()

    private val _startingFrameUri = MutableStateFlow<String?>(null)
    val startingFrameUri: StateFlow<String?> = _startingFrameUri.asStateFlow()

    private val _endingFrameUri = MutableStateFlow<String?>(null)
    val endingFrameUri: StateFlow<String?> = _endingFrameUri.asStateFlow()

    // Generation Settings
    private val _settings = MutableStateFlow(GenerationSettings())
    val settings: StateFlow<GenerationSettings> = _settings.asStateFlow()

    // Multi-Scene Builder
    private val _scenes = MutableStateFlow(
        listOf(
            SceneItem(1, 10, "A friendly elephant welcomes travellers at the village archway", "Gajraj", "Namaste! Welcome to our vibrant sanctuary.", "Hindi Voice"),
            SceneItem(2, 10, "Market vendors tossing rose petals and spices into the golden sunlight", "Meera", "Look at how the colors dance in the breeze!", "English Voice"),
            SceneItem(3, 10, "Sunset celebration with festival lamps reflecting in the lotus pond", "Gajraj", "Until we meet again under the starlight.", "Hindi Voice")
        )
    )
    val scenes: StateFlow<List<SceneItem>> = _scenes.asStateFlow()

    // Playback / Detail Inspection
    private val _selectedProject = MutableStateFlow<VideoProject?>(null)
    val selectedProject: StateFlow<VideoProject?> = _selectedProject.asStateFlow()

    // Status / Messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun setNavTab(tab: AppNavTab) {
        _currentNavTab.value = tab
    }

    fun setCreateMode(mode: GenerationMode) {
        _currentCreateMode.value = mode
    }

    fun setPromptText(text: String) {
        _promptText.value = text
        _enhancedPrompt.value = null // reset enhanced version on direct prompt edits
    }

    fun setEnhancedPrompt(text: String?) {
        _enhancedPrompt.value = text
    }

    fun enhancePrompt() {
        val current = _promptText.value.trim()
        if (current.isBlank()) {
            _errorMessage.value = "Please enter a prompt first to enhance."
            return
        }

        viewModelScope.launch {
            _isEnhancingPrompt.value = true
            try {
                val enhanced = enhancerService.enhancePrompt(
                    current,
                    _settings.value.style,
                    _settings.value.cameraMotion
                )
                _enhancedPrompt.value = enhanced
                _infoMessage.value = "Magic Prompt applied! You can review or edit before generation."
            } catch (e: Exception) {
                _errorMessage.value = "Prompt enhancement: ${e.localizedMessage}"
            } finally {
                _isEnhancingPrompt.value = false
            }
        }
    }

    fun applyEnhancedPromptToMain() {
        _enhancedPrompt.value?.let {
            _promptText.value = it
            _enhancedPrompt.value = null
            _infoMessage.value = "Enhanced prompt copied into main prompt box"
        }
    }

    fun setSelectedImageUri(uri: String?) {
        _selectedImageUri.value = uri
    }

    fun setCharacterReferenceUri(uri: String?) {
        _characterReferenceUri.value = uri
    }

    fun setStartingFrameUri(uri: String?) {
        _startingFrameUri.value = uri
    }

    fun setEndingFrameUri(uri: String?) {
        _endingFrameUri.value = uri
    }

    fun updateDuration(duration: DurationOption) {
        _settings.value = _settings.value.copy(duration = duration)
    }

    fun updateAspectRatio(ratio: AspectRatioOption) {
        _settings.value = _settings.value.copy(aspectRatio = ratio)
    }

    fun updateStyle(style: VideoStyleOption) {
        _settings.value = _settings.value.copy(style = style)
    }

    fun updateQuality(quality: VideoQualityOption) {
        _settings.value = _settings.value.copy(quality = quality)
    }

    fun updateCameraMotion(motion: CameraMotionOption) {
        _settings.value = _settings.value.copy(cameraMotion = motion)
    }

    fun updateMotionStrength(strength: MotionStrengthOption) {
        _settings.value = _settings.value.copy(motionStrength = strength)
    }

    fun updateNegativePrompt(negative: String) {
        _settings.value = _settings.value.copy(negativePrompt = negative)
    }

    fun setKeepCharacterConsistent(keep: Boolean) {
        _settings.value = _settings.value.copy(keepCharacterConsistent = keep)
    }

    fun updateVoiceConfig(update: (VoiceConfiguration) -> VoiceConfiguration) {
        _settings.value = _settings.value.copy(voiceConfig = update(_settings.value.voiceConfig))
    }

    fun updateProviderType(type: ProviderType) {
        _settings.value = _settings.value.copy(providerType = type)
        _infoMessage.value = "Selected Provider: ${type.displayName}"
    }

    fun updateBackendUrl(url: String) {
        repository.backendProvider.updateBaseUrl(url)
        _infoMessage.value = "Backend URL updated"
    }

    fun getBackendUrl(): String = repository.backendProvider.getBaseUrl()

    // Scene Builder methods
    fun addScene() {
        val nextNumber = _scenes.value.size + 1
        _scenes.value = _scenes.value + SceneItem(
            sceneNumber = nextNumber,
            durationSeconds = 10,
            prompt = "Scene $nextNumber continuation...",
            characterName = "Character $nextNumber",
            dialogueText = "",
            voiceType = "English Voice"
        )
    }

    fun updateScene(index: Int, updated: SceneItem) {
        val currentList = _scenes.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updated
            _scenes.value = currentList
        }
    }

    fun removeScene(index: Int) {
        if (_scenes.value.size <= 1) {
            _errorMessage.value = "A story must contain at least 1 scene."
            return
        }
        val currentList = _scenes.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            // Renumber
            _scenes.value = currentList.mapIndexed { idx, s -> s.copy(sceneNumber = idx + 1) }
        }
    }

    fun generateVideo() {
        val currentPrompt = _enhancedPrompt.value ?: _promptText.value.trim()
        val mode = _currentCreateMode.value

        // Validation
        if (mode == GenerationMode.TEXT_TO_VIDEO && currentPrompt.isBlank()) {
            _errorMessage.value = "Please enter a description for your video."
            return
        }

        if (mode == GenerationMode.IMAGE_TO_VIDEO && _selectedImageUri.value.isNullOrBlank()) {
            _errorMessage.value = "Please select or upload a reference image for Image-to-Video."
            return
        }

        if (mode == GenerationMode.SCENE_BUILDER && _scenes.value.isEmpty()) {
            _errorMessage.value = "Please add at least one scene to generate a multi-clip story."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = when (mode) {
                GenerationMode.TEXT_TO_VIDEO -> {
                    repository.startTextToVideo(
                        prompt = _promptText.value.trim(),
                        enhancedPrompt = _enhancedPrompt.value,
                        settings = _settings.value
                    )
                }
                GenerationMode.IMAGE_TO_VIDEO -> {
                    repository.startImageToVideo(
                        prompt = _promptText.value.trim().ifBlank { "Animate motion dynamically" },
                        imageUri = _selectedImageUri.value!!,
                        settings = _settings.value
                    )
                }
                GenerationMode.SCENE_BUILDER -> {
                    repository.startSceneStory(
                        scenes = _scenes.value,
                        settings = _settings.value
                    )
                }
            }

            _isSubmitting.value = false
            result.fold(
                onSuccess = { project ->
                    _selectedProject.value = project
                    _infoMessage.value = "Generation dispatched! Monitor progress below."
                    _currentNavTab.value = AppNavTab.PROJECTS
                },
                onFailure = { err ->
                    _errorMessage.value = "Generation failed: ${err.localizedMessage}"
                }
            )
        }
    }

    fun openProject(project: VideoProject) {
        _selectedProject.value = project
    }

    fun closePlayback() {
        _selectedProject.value = null
    }

    fun duplicateProject(project: VideoProject) {
        _promptText.value = project.prompt
        _enhancedPrompt.value = project.enhancedPrompt
        _selectedImageUri.value = project.imageReferenceUri
        _characterReferenceUri.value = project.characterReferenceUri
        _currentCreateMode.value = project.mode
        _currentNavTab.value = AppNavTab.CREATE
        _infoMessage.value = "Loaded settings from project into studio"
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_selectedProject.value?.id == id) {
                _selectedProject.value = null
            }
            _infoMessage.value = "Project deleted"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearInfo() {
        _infoMessage.value = null
    }
}
