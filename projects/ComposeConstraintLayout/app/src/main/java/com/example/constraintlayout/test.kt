package androidx.constraintlayout.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import com.example.constraintlayout.R
import java.util.*

@Preview
@Composable
public fun Screen() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (image, header, tag1, tag2, tag3, bSignup, bLogin, disclaimer) = createRefs()
        val g1 = createGuidelineFromStart(44.dp)
        val g2 = createGuidelineFromEnd(44.dp)
        Image(
            modifier = Modifier.constrainAs(image) {
                width = Dimension.value(201.dp)
                height = Dimension.value(179.dp)
                top.linkTo(parent.top, 32.dp)
                start.linkTo(g1)
            },
            painter = painterResource(id = R.drawable.intercom_snooze),
            contentDescription = null
        )
        Text(
            modifier = Modifier.constrainAs(header) {
                top.linkTo(image.bottom, 32.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h5,
        )
        Text(
            modifier = Modifier.constrainAs(tag1) {
                top.linkTo(header.bottom, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            text = stringResource(id = R.string.welcome_tagline1)
        )
        Text(
            modifier = Modifier.constrainAs(tag2) {
                top.linkTo(tag1.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            text = stringResource(id = R.string.welcome_tagline2)
        )
        Text(
            modifier = Modifier.constrainAs(tag3) {
                top.linkTo(tag2.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            text = stringResource(id = R.string.welcome_tagline3)
        )
        Button(
            modifier = Modifier.constrainAs(bSignup) {
                bottom.linkTo(bLogin.top, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            onClick = {}
        ) {
            Text(text = stringResource(id = R.string.sign_up))
        }
        Button(
            modifier = Modifier.constrainAs(bLogin) {
                bottom.linkTo(disclaimer.top, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(
            modifier = Modifier.constrainAs(disclaimer) {
                bottom.linkTo(parent.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            text = stringResource(id = R.string.trial_disclaimer),
            style = MaterialTheme.typography.caption,
        )
    }
}

@Preview
@Composable
public fun Screen2() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val bLogin = createRef()
        val g1 = createGuidelineFromStart(44.dp)
        val g2 = createGuidelineFromEnd(44.dp)
        Button(
            modifier = Modifier.constrainAs(bLogin) {
                bottom.linkTo(parent.bottom, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            },
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
    }
}

@Preview
@Composable
public fun Screen3() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val bLogin = createRef()
        val g1 = createGuidelineFromTop(44.dp)
        val g2 = createGuidelineFromBottom(44.dp)
        Button(
            modifier = Modifier.constrainAs(bLogin) {
                top.linkTo(g1)
                bottom.linkTo(g2)
                height = Dimension.fillToConstraints
            },
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
    }
}

@Preview
@Composable
public fun Screen4() {
    ConstraintLayout(
        ConstraintSet {
            val image = createRefFor("image")
            val header = createRefFor("header")
            val tag1 = createRefFor("tag1")
            val tag2 = createRefFor("tag2")
            val tag3 = createRefFor("tag3")
            val bSignup = createRefFor("bSignup")
            val bLogin = createRefFor("bLogin")
            val disclaimer = createRefFor("disclaimer")

            val g1 = createGuidelineFromStart(44.dp)
            val g2 = createGuidelineFromEnd(44.dp)

            constrain(image) {
                width = Dimension.value(201.dp)
                height = Dimension.value(179.dp)
                top.linkTo(parent.top, 32.dp)
                start.linkTo(g1)
            }

            constrain(header) {
                top.linkTo(image.bottom, 32.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(tag1) {
                top.linkTo(header.bottom, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(tag2) {
                top.linkTo(tag1.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(tag3) {
                top.linkTo(tag2.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(bSignup) {
                bottom.linkTo(bLogin.top, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(bLogin) {
                bottom.linkTo(disclaimer.top, 16.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }

            constrain(disclaimer) {
                bottom.linkTo(parent.bottom, 8.dp)
                start.linkTo(g1)
                end.linkTo(g2)
                width = Dimension.fillToConstraints
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(modifier = Modifier.layoutId("image"),
            painter = painterResource(id = R.drawable.intercom_snooze),
            contentDescription = null
        )
        Text(modifier = Modifier.layoutId("header"),
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h5,
        )
        Text(modifier = Modifier.layoutId("tag1"),
            text = stringResource(id = R.string.welcome_tagline1)
        )
        Text(modifier = Modifier.layoutId("tag2"),
            text = stringResource(id = R.string.welcome_tagline2)
        )
        Text(modifier = Modifier.layoutId("tag3"),
            text = stringResource(id = R.string.welcome_tagline3)
        )
        Button(modifier = Modifier.layoutId("bSignup"),
            onClick = {}
        ) {
            Text(text = stringResource(id = R.string.sign_up))
        }
        Button(modifier = Modifier.layoutId("bLogin"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(modifier = Modifier.layoutId("disclaimer"),
            text = stringResource(id = R.string.trial_disclaimer),
            style = MaterialTheme.typography.caption,
        )
    }
}

@Preview
@Composable
public fun Screen5() {
    val cs = """
        {
          g1 : { type: 'vGuideline', start: 44 },
          g2 : { type: 'vGuideline', end: 44 },
          image: {
            width: 201, height: 179,
            top: ['parent','top', 32],
            start: 'g1' 
          },
          header: {
            width: 'spread',
            start: ['g1', 'start'], end: ['g2', 'start'],
            top: ['image','bottom', 32]
          },
          tag1: {
            width: 'spread',
            start: 'g1', end: 'g2',
            top: ['header','bottom', 16]
          },
          tag2: {
            width: 'spread',
            start: 'g1', end: 'g2',
            top: ['tag1','bottom', 8]
          },
          tag3: {
            width: 'spread',
            start: 'g1', end: 'g2',
            top: ['tag2','bottom', 8]
          },
          bSignup: {
            width: 'spread',
            start: 'g1', end: 'g2',
            bottom: ['bLogin','top', 16]
          },
          bLogin: {
            width: 'spread',
            start: 'g1', end: 'g2',
            bottom: ['disclaimer','top', 16]
          },
          disclaimer: {
            width: 'spread',
            start: 'g1', end: 'g2',
            bottom: ['parent','bottom', 8]
          }
        }
    """
    ConstraintLayout(
        ConstraintSet(cs),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(modifier = Modifier.layoutId("image"),
            painter = painterResource(id = R.drawable.intercom_snooze),
            contentDescription = null
        )
        Text(modifier = Modifier.layoutId("header"),
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h5,
        )
        Text(modifier = Modifier.layoutId("tag1"),
            text = stringResource(id = R.string.welcome_tagline1)
        )
        Text(modifier = Modifier.layoutId("tag2"),
            text = stringResource(id = R.string.welcome_tagline2)
        )
        Text(modifier = Modifier.layoutId("tag3"),
            text = stringResource(id = R.string.welcome_tagline3)
        )
        Button(modifier = Modifier.layoutId("bSignup"),
            onClick = {}
        ) {
            Text(text = stringResource(id = R.string.sign_up))
        }
        Button(modifier = Modifier.layoutId("bLogin"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(modifier = Modifier.layoutId("disclaimer"),
            text = stringResource(id = R.string.trial_disclaimer),
            style = MaterialTheme.typography.caption,
        )
    }
}


@Preview
@Composable
public fun ScreenExample() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (button, title) = createRefs()
        val g1 = createGuidelineFromStart(80.dp)
        Button(
            modifier = Modifier.constrainAs(button) {
                top.linkTo(title.bottom, 16.dp)
                start.linkTo(g1)
            },
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(modifier = Modifier.constrainAs(title) {
            centerVerticallyTo(parent)
            start.linkTo(g1)
        },
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h2,
        )
    }
}

@Preview
@Composable
public fun ScreenExample2() {
    ConstraintLayout(
        ConstraintSet {
            val button = createRefFor("button")
            val title = createRefFor("title")
            val g1 = createGuidelineFromStart(80.dp)
            constrain(button) {
                top.linkTo(title.bottom, 16.dp)
                start.linkTo(g1)
            }
            constrain(title) {
                centerVerticallyTo(parent)
                start.linkTo(g1)
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier.layoutId("button"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(modifier = Modifier.layoutId("title"),
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h2,
        )
    }
}

@Preview(group = "new")
@Composable
public fun ScreenExample3() {
    ConstraintLayout(
        ConstraintSet("""
            {
                Debug: { name: 'example 3'},
                g1: { type: 'vGuideline', start: 80 },
                button: {
                  top: ['title', 'bottom', 16],
                  start: ['g1', 'start']
                },
                title: {
                  centerVertically: 'parent',
                  start: ['g1', 'start']
                }
            }
        """),
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            modifier = Modifier.layoutId("button"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
        Text(modifier = Modifier.layoutId("title"),
            text = stringResource(id = R.string.welcome_header),
            style = MaterialTheme.typography.h2,
        )
    }
}

@Preview(group = "new2")
@Composable
public fun ScreenExample4() {
    ConstraintLayout(
        ConstraintSet("""
            {
                Debug: { name: 'example 4'},
                g1: { type: 'vGuideline', percent: 0.5 },
                button: {
                  start: ['g1', 'start']
                }
            }
        """),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Button(
            modifier = Modifier.layoutId("button"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
    }
}

@Preview(group = "new3")
@Composable
public fun ScreenExample5() {
    ConstraintLayout(
        ConstraintSet("""
            {
              m1 : 100,
              barrier: { type: 'barrier', direction: 'end', contains: ['a','b'] },
              a: {
                width: 'wrap',
                height: 100,
                center: 'parent',
                top : ['parent', 'top', 'm1' ]
              },
              b: {
                start: ['parent', 'start', 'm1' ],
                top : ['a', 'bottom', 'm1' ]
              },
              c: {
                start:  ['barrier', 'start'],
                top : ['b', 'bottom', 'm1' ]
              }
            }
        """),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier.layoutId("a"),
            onClick = {},
        ) {
            Text(text = "A")
        }
        Button(
            modifier = Modifier.layoutId("b"),
            onClick = {},
        ) {
            Text(text = "B")
        }
        Button(
            modifier = Modifier.layoutId("c"),
            onClick = {},
        ) {
            Text(text = "C")
        }
    }
}

@Preview(group = "new4")
@Composable
public fun ScreenExample6() {
    ConstraintLayout(
        ConstraintSet("""
            {
              Variables: {
                bottom: 20
              },
              Helpers: [
                ['hChain', ['a','b','c'], {
                  start: ['leftGuideline1', 'start'],
                  style: 'packed'
                }],
                ['hChain', ['d','e','f']],
                ['vChain', ['d','e','f'], {
                  bottom: ['topGuideline1', 'top']
                }],
                ['vGuideline', {
                  id: 'leftGuideline1', start: 100
                }],
                ['hGuideline', {
                  id: 'topGuideline1', percent: 0.5
                }]
              ],
              a: {
                bottom: ['b', 'top', 'bottom']
              },
              b: {
                width: '30%',
                height: '1:1',
                centerVertically: 'parent'
              },
              c: {
                top: ['b', 'bottom']
              }
            }
        """),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier.layoutId("a"),
            onClick = {},
        ) {
            Text(text = "A")
        }
        Button(
            modifier = Modifier.layoutId("b"),
            onClick = {},
        ) {
            Text(text = "B")
        }
        Button(
            modifier = Modifier.layoutId("c"),
            onClick = {},
        ) {
            Text(text = "C")
        }
        Button(
            modifier = Modifier.layoutId("d"),
            onClick = {},
        ) {
            Text(text = "D")
        }
        Button(
            modifier = Modifier.layoutId("e"),
            onClick = {},
        ) {
            Text(text = "E")
        }
        Button(
            modifier = Modifier.layoutId("f"),
            onClick = {},
        ) {
            Text(text = "F")
        }
    }
}

@Preview(group = "motion1")
@Composable
public fun ScreenExample7() {
    var animateToEnd by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(2000)
    )
    Column {
     MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            start = ConstraintSet(
                """
            {
              a: {
                rotationY: 180,
                width: 'spread',
                start: ['parent', 'start', 16],
                end: ['parent', 'end', 16],
                bottom: ['parent', 'bottom', 16]
              },
              b: {
                alpha: 0.2,
                scaleX: 5,
                scaleY: 5,
                rotationZ: -30,
                end: ['parent', 'end', 64],
                bottom: ['a', 'top', 64]
              }
            }
        """
            ),
            end = ConstraintSet(
                """
            {
              a: {
                rotationZ: -30,
                end: ['parent', 'end', 16],
                top: ['parent', 'top', 16]
              },
              b: {
                scaleX: 10,
                centerHorizontally: 'parent',
                top: ['a', 'bottom', 16]
              }
            }
        """
            ),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress
        ) {
            Button(
                modifier = Modifier.layoutId("a"),
                onClick = {},
            ) {
                Text(text = "Hello")
            }
            Text(
                modifier = Modifier.layoutId("b"),
                text = "B"
            )
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion2")
@Composable
public fun ScreenExample8() {

    var cs1 = ConstraintSet("""
            {
              a: {
                rotationZ: -30,
                scaleX: 5,
                start: ['parent', 'start', 16],
                top: ['parent', 'top', 16]
              }
            }
    """)
    var cs2 = ConstraintSet("""
            {
              a: {
                rotationZ: 30,
                end: ['parent', 'end', 16],
                top: ['parent', 'top', 16]
              }
            }
    """)
    var cs3 = ConstraintSet("""
            {
              a: {
                end: ['parent', 'end', 16],
                bottom: ['parent', 'bottom', 16]
              }
            }
    """)
    var cs4 = ConstraintSet("""
            {
              a: {
                start: ['parent', 'start', 16],
                bottom: ['parent', 'bottom', 16]
              } 
            }
    """)

    var start by remember { mutableStateOf(cs1) }
    var end by remember { mutableStateOf(cs2) }
    var inTransition by remember { mutableStateOf(false) }
    var progress = remember { Animatable(0f) }
    var config by remember { mutableStateOf(0) }
    var started by remember { mutableStateOf(false) }

    if (started) {
        LaunchedEffect(config) {
            if (!inTransition) {
                inTransition = true
                progress.animateTo(
                    1f,
                    animationSpec = tween(2000)
                )
                inTransition = false
                progress.snapTo(0f)
                when (config) {
                    0 -> {
                        start = cs1
                        end = cs2
                    }
                    1 -> {
                        start = cs2
                        end = cs3
                    }
                    2 -> {
                        start = cs3
                        end = cs4
                    }
                    3 -> {
                        start = cs4
                        end = cs1
                    }
                }
            } else {
                inTransition = false
                progress.animateTo(
                    0f,
                    animationSpec = tween(2000)
                )
            }
        }
    }

    Column {
        Button(onClick = {
            started = true;
            if (!inTransition) {
                config = (config + 1) % 4
            } else if (config > 0) {
                config = (config - 1) % 4
            }
        }) {
            Text(text = "Run")
        }

        MotionLayout(
            modifier = Modifier
                .fillMaxSize(),
            start = start,
            end = end,
            debug = EnumSet.of(
                MotionLayoutDebugFlags.SHOW_ALL
            ),
            progress = progress.value
        ) {
            Button(
                modifier = Modifier.layoutId("a"),
                onClick = {},
            ) {
                Text(text = "Hello")
            }
        }
    }
}

@Preview(group = "new5")
@Composable
public fun ScreenExample9() {
    ConstraintLayout(
        ConstraintSet("""
            {
                center: {
                  center: 'parent'
                },
                h1: { circular: ['center', 30, 100] },
                h2: { circular: ['center', 60, 100] },
                h3: { circular: ['center', 90, 100] },
                h4: { circular: ['center', 120, 100] },
                h5: { circular: ['center', 150, 100] },
                h6: { circular: ['center', 180, 100] },
                h7: { circular: ['center', 210, 100] },
                h8: { circular: ['center', 240, 100] },
                h9: { circular: ['center', 270, 100] },
                h10: { circular: ['center', 300, 100] },
                h11: { circular: ['center', 330, 100] },
                h12: { circular: ['center', 0, 100] }
            }
        """),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(modifier = Modifier.layoutId("center"), text = "C")
        Text(modifier = Modifier.layoutId("h1"), text = "1")
        Text(modifier = Modifier.layoutId("h2"), text = "2")
        Text(modifier = Modifier.layoutId("h3"), text = "3")
        Text(modifier = Modifier.layoutId("h4"), text = "4")
        Text(modifier = Modifier.layoutId("h5"), text = "5")
        Text(modifier = Modifier.layoutId("h6"), text = "6")
        Text(modifier = Modifier.layoutId("h7"), text = "7")
        Text(modifier = Modifier.layoutId("h8"), text = "8")
        Text(modifier = Modifier.layoutId("h9"), text = "9")
        Text(modifier = Modifier.layoutId("h10"), text = "10")
        Text(modifier = Modifier.layoutId("h11"), text = "11")
        Text(modifier = Modifier.layoutId("h12"), text = "12")
    }
}

@Preview(group = "new6")
@Composable
public fun ScreenExample10() {
    ConstraintLayout(
        ConstraintSet("""
            {
                h1: { circular: ['parent', 0, 100] },
                h2: { circular: ['parent', 40, 100], rotationZ: 40 },
                h3: { circular: ['parent', 80, 100], rotationZ: 80 },
                h4: { circular: ['parent', 120, 100], rotationZ: 120  },
                h5: { circular: ['parent', 160, 100], rotationZ: 160  },
                h6: { circular: ['parent', 200, 100], rotationZ: 200  },
                h7: { circular: ['parent', 240, 100], rotationZ: 240  },
                h8: { circular: ['parent', 280, 100], rotationZ: 280  },
                h9: { circular: ['parent', 320, 100], rotationZ: 320  }
            }
        """),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(modifier = Modifier
            .layoutId("h1")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Red))
        Box(modifier = Modifier
            .layoutId("h2")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Green))
        Box(modifier = Modifier
            .layoutId("h3")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Blue))
        Box(modifier = Modifier
            .layoutId("h4")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Gray))
        Box(modifier = Modifier
            .layoutId("h5")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Yellow))
        Box(modifier = Modifier
            .layoutId("h6")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Cyan))
        Box(modifier = Modifier
            .layoutId("h7")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Magenta))
        Box(modifier = Modifier
            .layoutId("h8")
            .width(100.dp)
            .height(60.dp)
            .background(Color.Red))
        Box(modifier = Modifier
            .layoutId("h9")
            .width(100.dp)
            .height(60.dp)
            .background(Color.DarkGray))
    }
}

@Preview(group = "motion3")
@Composable
public fun ScreenExample11() {
    var animateToEnd by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(4000)
    )
    Column {
        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
        MotionLayout(
            ConstraintSet("""
            {
                h1: { circular: ['parent', 0, 100] },
                h2: { circular: ['parent', 40, 100], rotationZ: 40 },
                h3: { circular: ['parent', 80, 100], rotationZ: 80 },
                h4: { circular: ['parent', 120, 100], rotationZ: 120  },
                h5: { circular: ['parent', 160, 100], rotationZ: 160  },
                h6: { circular: ['parent', 200, 100], rotationZ: 200  },
                h7: { circular: ['parent', 240, 100], rotationZ: 240  },
                h8: { circular: ['parent', 280, 100], rotationZ: 280  },
                h9: { circular: ['parent', 320, 100], rotationZ: 320  }
            }
        """),
            ConstraintSet("""
            {
                h1: { circular: ['parent', 0, 100], rotationZ: 360 },
                h2: { circular: ['parent', 40, 100], rotationZ: 400 },
                h3: { circular: ['parent', 80, 100], rotationZ: 440 },
                h4: { circular: ['parent', 120, 100], rotationZ: 480  },
                h5: { circular: ['parent', 160, 100], rotationZ: 520  },
                h6: { circular: ['parent', 200, 100], rotationZ: 560  },
                h7: { circular: ['parent', 240, 100], rotationZ: 600  },
                h8: { circular: ['parent', 280, 100], rotationZ: 640  },
                h9: { circular: ['parent', 320, 100], rotationZ: 680  }
            }
        """),
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(modifier = Modifier.layoutId("h1").width(100.dp).height(60.dp).background(Color.Red))
            Box(modifier = Modifier.layoutId("h2").width(100.dp).height(60.dp).background(Color.Green))
            Box(modifier = Modifier.layoutId("h3").width(100.dp).height(60.dp).background(Color.Blue))
            Box(modifier = Modifier.layoutId("h4").width(100.dp).height(60.dp).background(Color.Gray))
            Box(modifier = Modifier.layoutId("h5").width(100.dp).height(60.dp).background(Color.Yellow))
            Box(modifier = Modifier.layoutId("h6").width(100.dp).height(60.dp).background(Color.Cyan))
            Box(modifier = Modifier.layoutId("h7").width(100.dp).height(60.dp).background(Color.Magenta))
            Box(modifier = Modifier.layoutId("h8").width(100.dp).height(60.dp).background(Color.Red))
            Box(modifier = Modifier.layoutId("h9").width(100.dp).height(60.dp).background(Color.DarkGray))
        }
    }
}

@Preview(group = "motion4")
@Composable
public fun ScreenExample12() {
    var animateToEnd by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(2000)
    )
    var baseConstraintSet = """
            {
                Variables: {
                  angle: { from: 0, step: 10 },
                  rotation: { from: 'startRotation', step: 10 },
                  distance: 100,
                  mylist: { tag: 'box' }
                },
                Generate: {
                  mylist: {
                    width: 200,
                    height: 40,
                    circular: ['parent', 'angle', 'distance'],
                    pivotX: 0.1,
                    pivotY: 0.1,
                    translationX: 225,
                    rotationZ: 'rotation'
                  }
                }
            }
        """

    var cs1 = ConstraintSet(baseConstraintSet, overrideVariables = "{ startRotation: 0 }")
    var cs2 = ConstraintSet(baseConstraintSet, overrideVariables = "{ startRotation: 90 }")

    Column {
        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
        MotionLayout(cs1, cs2,
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            var colors = arrayListOf<Color>(Color.Red, Color.Green, Color.Blue, Color.Cyan, Color.Yellow)

            for (i in 1..36) {
                Box(modifier = Modifier
                    .layoutId("h$i", "box")
                    .background(colors[i % colors.size]))
            }
        }
    }
}

@Preview(group = "motion5")
@Composable
public fun ScreenExample13() {
    var animateToEnd by remember { mutableStateOf(false) }

    val cprogress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(2000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White) ,
            start = ConstraintSet("""
            {
              a: {
                start: ['parent', 'start', 16],
                bottom: ['parent', 'bottom', 16],
                custom: {
                  background: '#FFFF00',
                  textColor: '#000000',
                  textSize: 64
                }
              }
            }
            """
            ),
            end = ConstraintSet(
                """
            {
              a: {
                end: ['parent', 'end', 16],
                top: ['parent', 'top', 16],
                rotationZ: 360,
                custom: {
                  background: '#0000FF',
                  textColor: '#FFFFFF',
                  textSize: 12
                }
              }
            }
            """
            ),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = cprogress) {
            var properties = motionProperties("a")
            Text(text = "Hello", modifier = Modifier
                .layoutId(properties.value.id())
                .background(properties.value.color("background"))
                ,color = properties.value.color("textColor")
                //,fontSize = properties.value.fontSize("textSize")
            )
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion6")
@Composable
public fun ScreenExample14() {
    var animateToEnd by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(20000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White) ,
            start = ConstraintSet("""
            {
              a: {
                width: 40,
                height: 40,
                start: ['parent', 'start', 16],
                bottom: ['parent', 'bottom', 16]
              }
            }
            """
            ),
            end = ConstraintSet(
                """
            {
              a: {
                width: 100,
                height: 100,
                end: ['parent', 'end', 16],
                top: ['parent', 'top', 16]
              }
            }
            """
            ),
            transition = Transition("""
            {
              KeyFrames: {
                KeyPositions: [
                {
                   target: ['a'],
                   frames: [50],
                   percentX: [0.8],
                   percentY: [0.8]
                }
                ]
              }
            }
            """),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress) {
            Box(modifier = Modifier
                .layoutId("a")
                .background(Color.Red))
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}


@Preview(group = "motion7")
@Composable
public fun ScreenExample15() {
    var animateToEnd by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(6000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White) ,
            start = ConstraintSet("""
            {
              a: {
                width: 40,
                height: 40,
                start: ['parent', 'start', 16],
                bottom: ['parent', 'bottom', 16]
              }
            }
            """
            ),
            end = ConstraintSet(
                """
            {
              a: {
                width: 150,
                height: 100,
                rotationZ: 390,
                end: ['parent', 'end', 16],
                top: ['parent', 'top', 16]
              }
            }
            """
            ),
            transition = Transition("""
            {
              KeyFrames: {
                KeyPositions: [
                {
                   target: ['a'],
                   frames: [25,50,75],
                   percentX: [0.1, 0.8, 0.1],
                   percentY: [0.4, 0.8, 0.0]
                }
                ]
              }
            }
            """),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress) {
            Box(modifier = Modifier
                .layoutId("a")
                .background(Color.Red))
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion8")
@Composable
public fun ScreenExample16() {
    var animateToEnd by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(6000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White),
            motionScene = MotionScene("""{
                Debug: {
                  name: 'motion8'
                },
                ConstraintSets: {
                  start: {
                    a: {
                      width: 40,
                      height: 40,
                      start: ['parent', 'start', 16],
                      bottom: ['parent', 'bottom', 16]
                    }
                  },
                  end: {
                    a: {
                      width: 150,
                      height: 100,
                      rotationZ: 390,
                      end: ['parent', 'end', 16],
                      top: ['parent', 'top', 16]
                    }
                  }
                },
                Transitions: {
                  default: {
                    from: 'start',
                    to: 'end',
                    KeyFrames: {
                      KeyPositions: [
                        {
                          target: ['a'],
                          frames: [25, 50, 75],
                          percentX: [0.1, 0.8, 0.1],
                          percentY: [0.4, 0.8, 0.0]
                        }
                      ]
                    }
                  }
                }
            }"""),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress) {
            Box(modifier = Modifier
                .layoutId("a")
                .background(Color.Red))
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion9")
@Composable
public fun ScreenExample17() {
    var animateToEnd by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(6000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White) ,
            start = ConstraintSet("""
            {
              a: {
                width: 40,
                height: 40,
                start: ['parent', 'start', 16],
                bottom: ['parent', 'bottom', 16]
              },
              b: {
                width: 40,
                height: 40,
                bottom: ['a', 'top', 16],
                start: ['a', 'end', 16]
              }
            }
            """
            ),
            end = ConstraintSet(
                """
            {
              a: {
                width: 40,
                height: 40,
                visibility: 'gone',
                end: ['parent', 'end', 100],
                top: ['parent', 'top', 100]
              },
              b: {
                width: 40,
                height: 40,
                top: ['a', 'bottom', 16, 20],
                end: ['a', 'start', 16, 20]
              }
            }
            """
            ),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress) {
            Box(modifier = Modifier
                .layoutId("a")
                .background(Color.Red))
            Box(modifier = Modifier
                .layoutId("b")
                .background(Color.Blue))
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion10")
@Composable
public fun ScreenExample18() {
    var animateToEnd by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(6000)
    )
    Column {
        MotionLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White),
            motionScene = MotionScene("""{
  Debug: {
    name: 'example 18'
  },
  ConstraintSets: {
    start: {
      a: {
        width: 100,
        height: 100,
        start: ['parent', 'start', 16],
        centerVertically: 'parent'
      },
      b: {
        width: 100,
        height: 100,
        start: ['parent', 'start', 36],
        top: ['a', 'top', 20],
        translationZ: 30
      }
    },
    end: {
      a: {
        width: 100,
        height: 100,
        translationZ: 30,
        end: ['parent', 'end', 16],
        centerVertically: 'parent'
      },
      b: {
        width: 100,
        height: 100,
        end: ['parent', 'end', 36],
        top: ['a', 'top', 20]
      }
    }
  }
}
"""),
            debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
            progress = progress) {
            Box(modifier = Modifier
                .layoutId("a")
                .background(Color.Red))
            Box(modifier = Modifier
                .layoutId("b")
                .background(Color.Green))
        }

        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
    }
}

@Preview(group = "motion11")
@Composable
public fun ScreenExample19() {
    var animateToEnd by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateToEnd) 1f else 0f,
        animationSpec = tween(2000)
    )

    Column {
        Button(onClick = { animateToEnd = !animateToEnd }) {
            Text(text = "Run")
        }
        MotionLayout(motionScene = MotionScene("""{
                Debug: {
                  name: 'exemple19'
                },
                ConstraintSets: {
                  start: {
                    Variables: {
                      angle: { from: 0, step: 10 },
                      rotation: { from: 0, step: 10 },
                      distance: 100,
                      mylist: { tag: 'box' },
                      test: { from: 1, to: 36, prefix: 'h' },
                    },
                    Generate: {
                      test: {
                        width: 200,
                        height: 40,
                        circular: ['parent', 'angle', 'distance'],
                        pivotX: 0.1,
                        pivotY: 0.1,
                        translationX: 225,
                        translationZ: 20, 
                        rotationZ: 'rotation'
                      },
                    }
                  },
                  end: {
                    Extends: 'start',
                    Variables: {
                      rotation: { from: 90, step: 10 }
                    }                    
                  }
                }
            }"""),
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            var colors = arrayListOf<Color>(Color.Red, Color.Green, Color.Blue, Color.Cyan, Color.Yellow)

            for (i in 1..36) {
                Box(modifier = Modifier
                    .layoutId("h$i", "box")
                    .background(colors[i % colors.size])
                )
            }
        }
    }
}

@Preview(group = "guidelines")
@Composable
public fun ScreenExample20() {
    ConstraintLayout(
        ConstraintSet("""
        {
          Debug: {
            name: 'guidelines'
          },
          Variables: {
            margin: 8
          },
          g1: {
            type: 'hGuideline',
            percent: 0.34
          },
          g2: {
            type: 'hGuideline',
            percent: 0.63
          },
          g5: {
            type: 'vGuideline',
            percent: 0.57
          },
          g6: {
            type: 'vGuideline',
            percent: 0.29
          },
          button: {
            width: 'spread',
            height: 'spread',
            top: ['g1', 'bottom', 'margin'],
            bottom: ['g2', 'top', 'margin'],
            start: ['g6', 'start', 'margin'],
            end: ['g5', 'end', 'margin']
          }
        }
        """),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Button(
            modifier = Modifier.layoutId("button"),
            onClick = {},
        ) {
            Text(text = stringResource(id = R.string.log_in))
        }
    }
}
















