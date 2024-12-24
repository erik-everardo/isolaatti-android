package com.isolaatti.posting.posts.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.isolaatti.MyApplication
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.databinding.FragmentMarkdownEditingBinding
import com.isolaatti.images.common.components.ImagesRow
import com.isolaatti.posting.link_creator.presentation.LinkCreatorViewModel
import com.isolaatti.posting.link_creator.ui.LinkCreatorFragment
import com.isolaatti.posting.posts.presentation.CreatePostViewModel
import java.io.File
import java.util.Calendar

class PostEditingFragment : Fragment(){
    companion object {
        const val LOG_TAG = "PostEditingFragment"
    }


    private lateinit var binding: FragmentMarkdownEditingBinding
    private val viewModel: CreatePostViewModel by activityViewModels()
    private val linkCreatorViewModel: LinkCreatorViewModel by viewModels()


    private val choosePictureLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if(it != null) {
            viewModel.addPicture(it)
        }
    }

    private fun makePhotoUri(): Uri {
        val cacheFile = File(requireContext().filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
        return FileProvider.getUriForFile(requireContext(), "${MyApplication.myApp.packageName}.provider", cacheFile)
    }

    private var cameraPhotoUri: Uri? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it && cameraPhotoUri != null) {
            viewModel.addPicture(cameraPhotoUri!!)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMarkdownEditingBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Log.d(LOG_TAG, event.toString())
            }
        })

        setupListeners()
        setupObservers()
        
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val pictures by viewModel.photos.collectAsState()
                IsolaattiTheme {
                    ImagesRow(
                        modifier = Modifier.padding(16.dp),
                        images = pictures,
                        deletable = true,
                        addable = true,
                        onClick = {

                        },
                        onDeleteClick = {
                            viewModel.removePicture(it)
                        },
                        onTakePicture = {
                            cameraPhotoUri = makePhotoUri()
                            takePhotoLauncher.launch(cameraPhotoUri)
                        },
                        onUploadPicture = {
                            choosePictureLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.filledTextField.editText?.setText(viewModel.content)
        binding.filledTextField.requestFocus()
        binding.filledTextField.editText?.doOnTextChanged { text, _, _, _ ->
            // make better validation :)
            viewModel.validation.postValue(!text.isNullOrEmpty())
            viewModel.content = text.toString()
        }
    }

    private fun setupObservers(){
        viewModel.postToEdit.observe(viewLifecycleOwner) {
            binding.filledTextField.editText?.setText(it.content)
        }
        linkCreatorViewModel.inserted.observe(viewLifecycleOwner) {
            if(it) {
                viewModel.content += " ${linkCreatorViewModel.markdown}"
                binding.filledTextField.editText?.setText(viewModel.content)
                linkCreatorViewModel.inserted.value = false
            }
        }

        viewModel.audioAttachment.observe(viewLifecycleOwner) { playable ->

        }
    }
}