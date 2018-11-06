import React, { Component } from 'react'
import { StyleProp, StyleSheet, Text, TouchableOpacity, ViewStyle } from 'react-native'
import { STYLES } from '../../styles'

type ButtonConnectProps = {
  title: string,
  disabled: boolean,
  onPress: () => void
}

export default class ButtonConnect extends Component<ButtonConnectProps> {
  public render () {
    let buttonStylesDisabled: StyleProp<ViewStyle>
    let textStylesDisabled: StyleProp<ViewStyle>

    if (this.props.disabled) {
      buttonStylesDisabled = styles.disabledRoot
      textStylesDisabled = styles.disabledButtonContent
    }

    return (
      <TouchableOpacity
        activeOpacity={0.6}
        style={[styles.root, buttonStylesDisabled]}
        onPress={this.props.onPress}
      >
        <Text style={[styles.buttonContent, textStylesDisabled]}>
          {this.props.title}
        </Text>
      </TouchableOpacity>
    )
  }
}

const styles = StyleSheet.create({
  root: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: STYLES.PADDING,
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 1
    },
    shadowOpacity: 0.35,
    shadowRadius: 20,
    borderColor: STYLES.COLOR_MAIN,
    borderWidth: 1,
    backgroundColor: STYLES.COLOR_BACKGROUND
  },
  disabledRoot: {
    borderColor: STYLES.COLOR_DISABLED
  },
  buttonContent: {
    fontSize: STYLES.FONT_NORMAL,
    color: STYLES.COLOR_MAIN
  },
  disabledButtonContent: {
    color: STYLES.COLOR_DISABLED
  }
})
