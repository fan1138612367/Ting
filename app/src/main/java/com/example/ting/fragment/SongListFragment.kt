package com.example.ting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.ting.databinding.FragmentSongListBinding
import com.example.ting.model.SongList
import com.example.ting.ui.theme.TingTheme
import com.example.ting.viewmodel.TingViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SongListFragment : Fragment() {
    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<SongListFragmentArgs>()
    private val viewModel by activityViewModels<TingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TingTheme(false) {
                    LaunchedEffect(args.id) {
                        viewModel.getSongList(args.id)
                    }

                    val lazyListState = rememberLazyListState()
                    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                    val songList by viewModel.songList.collectAsState()
                    Scaffold(
                        topBar = {
                            SmallTopAppBar(
                                modifier = Modifier.padding(
                                    rememberInsetsPaddingValues(
                                        insets = LocalWindowInsets.current.statusBars,
                                        applyBottom = false
                                    )
                                ),
                                title = {
                                    Text(text = "声音详情")
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        findNavController().navigateUp()
                                    }) {
                                        Icon(Icons.Rounded.ArrowBack, "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                                scrollBehavior = scrollBehavior
                            )
                        },
                        floatingActionButton = {
                            if (lazyListState.firstVisibleItemIndex > 10) {
                                FloatingActionButton(
                                    modifier = Modifier.navigationBarsPadding(),
                                    onClick = {
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            lazyListState.scrollToItem(0)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.ArrowUpward, null)
                                }
                            }
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                            state = lazyListState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                SongInfo(songList.playlist)
                            }
                            item {
                                SongIcon(viewModel, songList.playlist)
                            }
                            itemsIndexed(songList.playlist.tracks) { index, item ->
                                SongList(index = index, track = item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
private fun SongInfo(
    playlist: SongList.Playlist
) {
    var showPlaylistDetailDialog by remember { mutableStateOf(false) }
    if (showPlaylistDetailDialog) {
        AlertDialog(
            onDismissRequest = {
                showPlaylistDetailDialog = false
            },
            title = {
                Text(text = playlist.name.ifBlank { "歌单信息" })
            },
            text = {
                SelectionContainer {
                    Text(text = playlist.description.ifBlank { "加载中" })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPlaylistDetailDialog = false
                }) {
                    Text(text = "关闭")
                }
            }
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = rememberAsyncImagePainter(model = playlist.coverImgUrl)
        Image(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .placeholder(
                    visible = painter.state is AsyncImagePainter.State.Loading,
                    highlight = PlaceholderHighlight.shimmer()
                ),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    showPlaylistDetailDialog = true
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = playlist.name.ifBlank { "加载中" },
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = playlist.creator.nickname.ifBlank { "加载中" },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = playlist.description.ifBlank { "歌单描述" },
                overflow = TextOverflow.Ellipsis,
                maxLines = 4,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SongIcon(
    viewModel: TingViewModel,
    playlist: SongList.Playlist
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = {
//            context.asyncGetSessionPlayer(MusicService::class.java) {
//                it.apply {
//                    scope.launch {
//                        stop()
//                        clearMediaItems()
//                        val mediaList = withContext(Dispatchers.Default) {
//                            playlistDetail.readSafely()?.playlist?.tracks?.map { track ->
//                                buildMediaItem(track.id.toString()) {
//                                    metadata {
//                                        setTitle(track.name)
//                                        setArtist(track.ar.joinToString(", ") { ar -> ar.name })
//                                        setMediaUri(Uri.parse("$RainMusicProtocol://music?id=${track.id}"))
//                                        setArtworkUri(Uri.parse(track.al.picUrl.https))
//                                    }
//                                }
//                            } ?: emptyList()
//                        }
//                        addMediaItems(mediaList)
//                        prepare()
//                        play()
//                        context.toast("开始播放该歌单")
//                    }
//                }
//            }
        }) {
            Text(text = "播放")
        }

        IconButton(onClick = {
            viewModel.subscribe(playlist.id, playlist.subscribed)
        }) {
            Icon(
                imageVector = if (playlist.subscribed) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Rounded.FavoriteBorder
                },
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = "共 ${playlist.trackCount} 首声音",
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SongList(
    index: Int, track: SongList.Playlist.Track
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${index + 1}")
            val painter = rememberAsyncImagePainter(model = track.al.picUrl)
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .placeholder(
                        visible = painter.state is AsyncImagePainter.State.Loading,
                        highlight = PlaceholderHighlight.shimmer()
                    )
                    .size(50.dp),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = track.ar.joinToString(separator = "/") { it.name } + if (track.al.name.isNotBlank()) " - ${track.al.name}" else "",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = {
//                context.asyncGetSessionPlayer(MusicService::class.java) {
//                    it.apply {
//                        stop()
//                        clearMediaItems()
//                        addMediaItem(
//                            buildMediaItem(track.id.toString()) {
//                                metadata {
//                                    setTitle(track.name)
//                                    setArtist(track.ar.joinToString(", ") { ar -> ar.name })
//                                    setMediaUri(Uri.parse("$RainMusicProtocol://music?id=${track.id}"))
//                                    setArtworkUri(Uri.parse(track.al.picUrl))
//                                }
//                            }
//                        )
//                        prepare()
//                        play()
//                    }
//                }
            }) {
                Icon(Icons.Rounded.PlayArrow, null)
            }
        }
    }
}